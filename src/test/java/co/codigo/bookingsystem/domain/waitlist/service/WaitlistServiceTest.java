package co.codigo.bookingsystem.domain.waitlist.service;

import co.codigo.bookingsystem.common.enumerations.BookingStatus;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @InjectMocks
    private WaitlistService waitlistService;

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ClassScheduleService classService;

    @Mock
    private PurchasedPackageService packageService;

    @Mock
    private UserService userService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    private final Long userId = 1L;
    private final Long classId = 2L;
    private final Long packageId = 3L;
    private final String countryCode = "SG";

    @Test
    void addToWaitlist_shouldAddUserToWaitlist() {
        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setId(classId);

        when(classService.getClassWithLock(classId)).thenReturn(classSchedule);
        when(bookingRepository.existsByUserIdAndBookedClassIdAndStatus(userId, classId, BookingStatus.CONFIRMED)).thenReturn(false);
        when(waitlistRepository.existsByUserIdAndWaitingClassId(userId, classId)).thenReturn(false);
        when(userService.getUserById(userId)).thenReturn(new User());

        Waitlist waitlist = waitlistService.addToWaitlist(userId, classId);

        assertNotNull(waitlist);
        verify(waitlistRepository).save(waitlist);
    }

    @Test
    void addToWaitlist_shouldThrowConflictExceptionIfAlreadyBooked() {
        when(bookingRepository.existsByUserIdAndBookedClassIdAndStatus(userId, classId, BookingStatus.CONFIRMED))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> waitlistService.addToWaitlist(userId, classId));
    }

    @Test
    void processWaitlist_shouldProcessSuccessfully() {
        String lockKey = "waitlist_lock:" + classId;

        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setId(classId);
        classSchedule.setMaxCapacity(10);
        classSchedule.setRequiredCredits(5);

        Waitlist waitlist = new Waitlist();
        User user = new User();
        waitlist.setUser(user);
        waitlist.setWaitingClass(classSchedule);

        PurchasedPackage purchasedPackage = new PurchasedPackage();
        purchasedPackage.setId(packageId);
        purchasedPackage.setRemainingCredits(10);

        when(redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(10))).thenReturn(true);
        when(classService.getClassWithLock(classId)).thenReturn(classSchedule);
        when(waitlistRepository.findTopByWaitingClassIdOrderByAddedAtAsc(classId)).thenReturn(Optional.of(waitlist));
        when(packageService.getUserValidPackages(user.getId(), classSchedule.getCountryCode())).thenReturn(List.of(purchasedPackage));
        when(bookingRepository.save(any(Booking.class))).thenReturn(new Booking());

        waitlistService.processWaitlist(classId);

        verify(bookingRepository).save(any(Booking.class));
        verify(waitlistRepository).delete(waitlist);
    }

    @Test
    void refundExpiredWaitlists_shouldRefundCredits() {
        ClassSchedule endedClass = new ClassSchedule();
        endedClass.setId(classId);
        endedClass.setRequiredCredits(5);

        Waitlist waitlist = new Waitlist();
        User user = new User();
        waitlist.setUser(user);
        waitlist.setWaitingClass(endedClass);

        PurchasedPackage purchasedPackage = new PurchasedPackage();
        purchasedPackage.setId(packageId);
        purchasedPackage.setRemainingCredits(10);

        when(classService.findAllEndedClasses()).thenReturn(List.of(endedClass));
        when(waitlistRepository.findByWaitingClassId(endedClass.getId())).thenReturn(List.of(waitlist));
        when(packageService.getUserValidPackages(user.getId(), endedClass.getCountryCode())).thenReturn(List.of(purchasedPackage));

        waitlistService.refundExpiredWaitlists();

        verify(packageService).refundCredits(packageId, endedClass.getRequiredCredits());
        verify(waitlistRepository).delete(waitlist);
    }

    @Test
    void refundExpiredWaitlists_shouldNotRefundIfNoPackage() {
        ClassSchedule endedClass = new ClassSchedule();
        endedClass.setId(classId);
        endedClass.setRequiredCredits(5);

        Waitlist waitlist = new Waitlist();
        waitlist.setUser(new User());
        waitlist.setWaitingClass(endedClass);

        when(classService.findAllEndedClasses()).thenReturn(List.of(endedClass));
        when(waitlistRepository.findByWaitingClassId(endedClass.getId())).thenReturn(List.of(waitlist));
        when(packageService.getUserValidPackages(any(), any())).thenReturn(Collections.emptyList());

        waitlistService.refundExpiredWaitlists();

        verify(packageService, never()).refundCredits(anyLong(), anyInt());
        verify(waitlistRepository).delete(waitlist);
    }
}