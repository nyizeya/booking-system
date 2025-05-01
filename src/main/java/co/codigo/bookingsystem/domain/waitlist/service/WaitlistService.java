package co.codigo.bookingsystem.domain.waitlist.service;

import co.codigo.bookingsystem.common.enumerations.BookingStatus;
import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.domain.availableclass.entity.AvailableClass;
import co.codigo.bookingsystem.domain.availableclass.service.AvailableClassService;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistService {
    
    private final WaitlistRepository waitlistRepository;
    private final BookingRepository bookingRepository;
    private final AvailableClassService classService;
    private final PurchasedPackageService packageService;
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate; // For concurrency control
    
    private static final String WAITLIST_LOCK_PREFIX = "waitlist_lock:";
    
    @Transactional
    public Waitlist addToWaitlist(Long userId, Long classId) {
        AvailableClass availableClass = classService.getClassWithLock(classId);
        
        if (bookingRepository.existsByUserIdAndBookedClassIdAndStatus(userId, classId, BookingStatus.CONFIRMED)) {
            throw new ConflictException("User already booked this class");
        }
        
        if (waitlistRepository.existsByUserIdAndWaitingClassId(userId, classId)) {
            throw new ConflictException("User already in waitlist");
        }
        
        Waitlist waitlist = new Waitlist();
        waitlist.setUser(userService.getUserById(userId)); // Proxy reference
        waitlist.setWaitingClass(availableClass);
        
        return waitlistRepository.save(waitlist);
    }
    
    @Transactional
    public void processWaitlist(Long classId) {
        String lockKey = WAITLIST_LOCK_PREFIX + classId;
        
        try {
            // Acquire distributed lock
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                lockKey, "locked", Duration.ofSeconds(10));
            
            if (Boolean.TRUE.equals(locked)) {
                AvailableClass availableClass = classService.getClassWithLock(classId);
                int availableSpots = calculateAvailableSpots(availableClass);
                
                while (availableSpots > 0) {
                    Optional<Waitlist> nextUser = waitlistRepository
                        .findTopByWaitingClassIdOrderByAddedAtAsc(classId);
                    
                    if (nextUser.isEmpty()) break;
                    
                    // Attempt to book
                    try {
                        User user = nextUser.get().getUser();
                        PurchasedPackage pkg = packageService.getUserValidPackages(
                            user.getId(), 
                            availableClass.getCountryCode()
                        ).stream().findFirst()
                         .orElseThrow(() -> new BusinessRuleException("No valid package"));
                        
                        Booking booking = new Booking();
                        booking.setUser(user);
                        booking.setBookedClass(availableClass);
                        booking.setStatus(BookingStatus.CONFIRMED);
                        bookingRepository.save(booking);
                        
                        packageService.deductCredits(pkg.getId(), availableClass.getRequiredCredits());
                        waitlistRepository.delete(nextUser.get());
                        availableSpots--;
                        
                    } catch (Exception e) {
                        // Skip this user if booking fails
                        waitlistRepository.delete(nextUser.get());
                        log.error("Failed to promote waitlist user: {}", e.getMessage());
                    }
                }
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
    
    // Refund credits to waitlisted users after class ends
    @Scheduled(cron = "0 0/5 * * * ?") // Runs every 5 minutes
    @Transactional
    public void refundExpiredWaitlists() {
        List<AvailableClass> endedClasses = classService.findAllEndedClasses();
        
        for (AvailableClass endedClass : endedClasses) {
            List<Waitlist> waitlists = waitlistRepository.findByWaitingClassId(endedClass.getId());
            
            for (Waitlist waitlist : waitlists) {
                try {
                    List<PurchasedPackage> packages = packageService.getUserValidPackages(
                        waitlist.getUser().getId(),
                        endedClass.getCountryCode()
                    );
                    
                    if (!packages.isEmpty()) {
                        packageService.refundCredits(
                            packages.get(0).getId(),
                            endedClass.getRequiredCredits()
                        );
                    }
                } finally {
                    waitlistRepository.delete(waitlist);
                }
            }
        }
    }
    
    private int calculateAvailableSpots(AvailableClass availableClass) {
        long bookedCount = bookingRepository.countByBookedClassIdAndStatus(
            availableClass.getId(), 
            BookingStatus.CONFIRMED
        );
        return availableClass.getMaxCapacity() - (int) bookedCount;
    }
}