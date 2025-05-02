package co.codigo.bookingsystem.domain.waitlist.service;

import co.codigo.bookingsystem.common.enumerations.BookingStatus;
import co.codigo.bookingsystem.common.enumerations.WaitlistStatus;
import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.common.exceptions.ConcurrencyException;
import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.classschedule.service.ClassScheduleService;
import co.codigo.bookingsystem.domain.booking.entity.Booking;
import co.codigo.bookingsystem.domain.booking.repository.BookingRepository;
import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import co.codigo.bookingsystem.domain.purchasedpkg.service.PurchasedPackageService;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.service.UserService;
import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import co.codigo.bookingsystem.domain.waitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final BookingRepository bookingRepository;
    private final ClassScheduleService classService;
    private final PurchasedPackageService packageService;
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String WAITLIST_LOCK_PREFIX = "waitlist_lock:";

    @Transactional
    public Waitlist addToWaitlist(Long userId, Long classId) {
        ClassSchedule classSchedule = classService.getClassWithLock(classId);

        // Prevent duplicate booking
        if (bookingRepository.existsByUserIdAndBookedClassIdAndStatus(userId, classId, BookingStatus.CONFIRMED)) {
            throw new ConflictException("User already booked this class");
        }

        // Prevent duplicate waitlist entry
        if (waitlistRepository.existsByUserIdAndWaitingClassId(userId, classId)) {
            throw new ConflictException("User already in waitlist");
        }

        Waitlist waitlist = new Waitlist();
        waitlist.setUser(userService.getUserById(userId));
        waitlist.setWaitingClass(classSchedule);
        waitlist.setStatus(WaitlistStatus.PENDING);

        return waitlistRepository.save(waitlist);
    }

    @Transactional
    public void processWaitlist(Long classId) {
        String lockKey = WAITLIST_LOCK_PREFIX + classId;

        try {
            // Acquire Redis lock
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                    lockKey, "locked", Duration.ofSeconds(10));

            if (!Boolean.TRUE.equals(locked)) {
                throw new ConcurrencyException("Waitlist processing in progress");
            }

            ClassSchedule classSchedule = classService.getClassWithLock(classId);
            int availableSpots = calculateAvailableSpots(classSchedule);

            while (availableSpots > 0) {
                Optional<Waitlist> nextUserOpt = waitlistRepository
                        .findTopByWaitingClassIdAndStatusOrderByAddedAtAsc(classId, WaitlistStatus.PENDING);

                if (nextUserOpt.isEmpty()) break;

                Waitlist waitlistEntry = nextUserOpt.get();
                User user = waitlistEntry.getUser();

                try {
                    // Find valid package with enough credits
                    PurchasedPackage pkg = packageService.getUserValidPackages(
                                    user.getId(),
                                    classSchedule.getCountryCode()
                            ).stream()
                            .filter(p -> p.getRemainingCredits() >= classSchedule.getRequiredCredits())
                            .findFirst()
                            .orElseThrow(() -> new BusinessRuleException("No valid package with enough credits"));

                    // Deduct credits first
                    packageService.deductCredits(pkg.getId(), classSchedule.getRequiredCredits());

                    // Create booking
                    Booking booking = new Booking();
                    booking.setUser(user);
                    booking.setBookedClass(classSchedule);
                    booking.setPurchasedPackage(pkg);
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);

                    // Remove user from waitlist
                    waitlistRepository.delete(waitlistEntry);

                    availableSpots--;

                    log.info("User {} promoted from waitlist to booking for class {}", user.getId(), classId);

                } catch (Exception e) {
                    waitlistEntry.setStatus(WaitlistStatus.CANCELLED);
                    waitlistRepository.save(waitlistEntry);
                    log.error("Failed to promote waitlist user {}: {}", user.getId(), e.getMessage(), e);
                }
            }

        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private int calculateAvailableSpots(ClassSchedule classSchedule) {
        long bookedCount = bookingRepository.countByBookedClassIdAndStatus(
                classSchedule.getId(),
                BookingStatus.CONFIRMED
        );
        return classSchedule.getMaxCapacity() - (int) bookedCount;
    }
}