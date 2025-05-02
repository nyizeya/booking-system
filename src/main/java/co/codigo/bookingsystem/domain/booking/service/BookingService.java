package co.codigo.bookingsystem.domain.booking.service;

import co.codigo.bookingsystem.common.enumerations.BookingStatus;
import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.common.exceptions.ConcurrencyException;
import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.common.utils.CommonUtils;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.classschedule.service.ClassScheduleService;
import co.codigo.bookingsystem.domain.booking.entity.Booking;
import co.codigo.bookingsystem.domain.booking.repository.BookingRepository;
import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import co.codigo.bookingsystem.domain.purchasedpkg.service.PurchasedPackageService;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.service.UserService;
import co.codigo.bookingsystem.domain.waitlist.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ClassScheduleService classScheduleService;
    private final PurchasedPackageService purchasedPackageService;
    private final UserService userService;
    private final WaitlistService waitlistService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BOOKING_LOCK_PREFIX = "booking:lock:";
    private static final Duration BOOKING_LOCK_TIMEOUT = Duration.ofSeconds(10);

    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Booking> getUpcomingUserBookings(Long userId) {
        return bookingRepository.findUpcomingByUser(userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Booking> getConfirmedBookingsForClass(Long classId) {
        return bookingRepository.findConfirmedBookingsForClass(classId);
    }

    @Transactional
    public Booking createBooking(Long userId, Long classId, Long packageId) {
        String lockKey = BOOKING_LOCK_PREFIX + classId + ":" + userId;

        try {
            // Acquire distributed lock
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", BOOKING_LOCK_TIMEOUT);
            if (!Boolean.TRUE.equals(locked)) {
                throw new ConcurrencyException("Booking operation in progress. Please try again.");
            }

            // Check duplicate booking
            if (bookingRepository.existsByUserIdAndBookedClassIdAndStatus(userId, classId, BookingStatus.CONFIRMED)) {
                throw new ConflictException("You already have an active booking for this class");
            }

            ClassSchedule classSchedule = classScheduleService.getClassWithLock(classId);
            User user = userService.getUserById(userId);

            // Check capacity
            if (isClassFull(classId)) {
                throw new ConflictException("Class is fully booked");
            }

            // Check overlapping booking
            if (hasOverlappingBookings(userId, classSchedule.getStartTime(), classSchedule.getEndTime())) {
                throw new ConflictException("You have another booking at this time");
            }

            // Validate package & deduct credits
            PurchasedPackage purchasedPackage = purchasedPackageService
                    .getPackageForBooking(userId, packageId, classSchedule.getCountryCode());

            purchasedPackageService.deductCredits(purchasedPackage.getId(), classSchedule.getRequiredCredits());

            // Save booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setBookedClass(classSchedule);
            booking.setPurchasedPackage(purchasedPackage); // FIXED: store package reference
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setBookedAt(LocalDateTime.now());

            return bookingRepository.save(booking);

        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> CommonUtils.createEntityNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessRuleException("Only confirmed bookings can be cancelled");
        }

        LocalDateTime cancellationDeadline = booking.getBookedClass().getStartTime().minusHours(4);
        boolean isRefundable = LocalDateTime.now().isBefore(cancellationDeadline);

        // Optimistic cancel
        int updated = bookingRepository.cancelBooking(bookingId, LocalDateTime.now());
        if (updated == 0) {
            throw new ConcurrencyException("Booking status changed during cancellation");
        }

        // Refund credits
        if (isRefundable) {
            purchasedPackageService.refundCredits(
                    booking.getPurchasedPackage().getId(),
                    booking.getBookedClass().getRequiredCredits()
            );
        }

        // Process waitlist
        if (isClassFull(booking.getBookedClass().getId())) {
            waitlistService.processWaitlist(booking.getBookedClass().getId());
        }

        return bookingRepository.findById(bookingId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public boolean isClassFull(Long classId) {
        ClassSchedule classSchedule = classScheduleService.getClassWithLock(classId);
        long bookedCount = bookingRepository.countByBookedClassIdAndStatus(classId, BookingStatus.CONFIRMED);
        return bookedCount >= classSchedule.getMaxCapacity();
    }

    @Transactional(readOnly = true)
    public boolean hasOverlappingBookings(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Booking> overlapping = bookingRepository.findUserBookingsBetweenDates(
                userId,
                startTime.minusMinutes(1),
                endTime.plusMinutes(1)
        );
        return overlapping.stream().anyMatch(b -> b.getStatus() == BookingStatus.CONFIRMED);
    }

    @Transactional(readOnly = true)
    public int getClassOccupancy(Long classId) {
        return bookingRepository.countConfirmedBookingsForClass(classId);
    }

    @Transactional(readOnly = true)
    public boolean isUserBooked(Long userId, Long classId) {
        return bookingRepository.existsByUserIdAndBookedClassIdAndStatus(userId, classId, BookingStatus.CONFIRMED);
    }
}