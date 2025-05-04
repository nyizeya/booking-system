package co.codigo.bookingsystem.domain.booking.service;

import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.common.services.RedisLockService;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.classschedule.service.ClassScheduleService;
import co.codigo.bookingsystem.domain.booking.entity.Booking;
import co.codigo.bookingsystem.domain.booking.repository.BookingRepository;
import co.codigo.bookingsystem.domain.packageplan.entity.UserPackage;
import co.codigo.bookingsystem.domain.packageplan.service.UserPackageService;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.service.UserService;
import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import co.codigo.bookingsystem.domain.waitlist.service.WaitlistService;
import co.codigo.bookingsystem.web.dtos.requests.BookingRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final UserService userService;
    private final WaitlistService waitlistService;
    private final RedisLockService redisLockService;
    private final BookingRepository bookingRepository;
    private final UserPackageService userPackageService;
    private final ClassScheduleService classScheduleService;

    public Booking findBookedClass(Long userId, Long bookedClassId) {
        return bookingRepository.findByUserIdAndBookedClassId(userId, bookedClassId).orElseThrow(()
                -> new EntityNotFoundException("Booking not found for userId [%d] and classId [%d]".formatted(userId, bookedClassId)));
    }

    @Transactional
    public Booking createBooking(Long userId, BookingRequest bookingRequest) {
        Long classId = bookingRequest.getClassId();
        Long packageId = bookingRequest.getPackageId();

        String lockKey = "user" + userId + ":class" + classId + ":package" + packageId + ":lock";
        String lockValue = UUID.randomUUID().toString();
        long lockExpireMs = 5000;
        boolean lockAcquired = false;

        try {
            lockAcquired = redisLockService.acquireLock(lockKey, lockValue, lockExpireMs);
            if (!lockAcquired) {
                throw new BusinessRuleException("Unable to secure a booking slot. Please try again.");
            }

            User user = userService.getUserById(userId);
            ClassSchedule classSchedule = classScheduleService.getClassScheduleById(classId);
            UserPackage userPackage = userPackageService.findByUserIdAndPackageId(userId, packageId);

            if (!userPackage.getPackagePlan().getCountryCode().equals(classSchedule.getCountryCode())) {
                throw new ConflictException("Package and class must be in the same country");
            }

            if (bookingRepository.existsByUserIdAndBookedClassId(user.getId(), classId)) {
                throw new ConflictException("You already have an active booking for this class");
            }

            if (isClassFull(classSchedule)) {
                throw new BusinessRuleException("Sorry, class schedule is full of specified amount of members. You may add it to waitlist.");
            }

            List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(userId, classSchedule.getStartTime(), classSchedule.getEndTime());

            if (!overlappingBookings.isEmpty()) {
                throw new ConflictException("You already have another booking that overlaps with this class time.");
            }

            userPackageService.deductCredits(user.getId(), packageId, classSchedule.getRequiredCredits());

            Booking booking = new Booking();
            booking.setUser(user);
            booking.setBookedClass(classSchedule);
            booking.setPackagePlan(userPackage.getPackagePlan());
            booking.setBookedAt(LocalDateTime.now());

            return bookingRepository.save(booking);
        } finally {
            if (lockAcquired)
                redisLockService.releaseLock(lockKey, lockValue);
        }
    }

    @Transactional
    public void cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found for userId [%d] and bookingId [%d]".formatted(userId, bookingId)));

        LocalDateTime cancellationDeadline = booking.getBookedClass().getStartTime().minusHours(4);
        boolean isRefundable = LocalDateTime.now().isBefore(cancellationDeadline);

        if (isRefundable) {
            userPackageService.refundCredits(userId, booking.getPackagePlan().getId(), booking.getBookedClass().getRequiredCredits());
        }

        if (isClassFull(booking.getBookedClass()) && booking.getBookedClass().getStartTime().isBefore(LocalDateTime.now())) {
            deleteBooking(bookingId);
            Optional<Waitlist> waitlistOptional = waitlistService.findTheOldestEntry();

            if(waitlistOptional.isPresent()) {
                Waitlist oldestWaitlist = waitlistOptional.get();
                log.info("Auto-booking class [{}] for waitlisted user [{}]", booking.getBookedClass().getId(), oldestWaitlist.getUser().getId());
                Booking newBooking = new Booking(
                        oldestWaitlist.getUser(),
                        booking.getBookedClass(),
                        oldestWaitlist.getPackagePlan(),
                        LocalDateTime.now()
                );

                bookingRepository.save(newBooking);
                waitlistService.removeFromWaitlist(oldestWaitlist);
            }

            return;
        }

        deleteBooking(bookingId);
    }

    @Transactional
    public void deleteBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Transactional(readOnly = true)
    public boolean isClassFull(ClassSchedule classSchedule) {
        long bookedCount = bookingRepository.countByBookedClassId(classSchedule.getId());
        return bookedCount >= classSchedule.getMaxCapacity();
    }
}