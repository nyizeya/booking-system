package co.codigo.bookingsystem.domain.booking.service;

import co.codigo.bookingsystem.common.enumerations.BookingStatus;
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
import co.codigo.bookingsystem.domain.waitlist.service.WaitlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ClassScheduleService classScheduleService;

    @Mock
    private PurchasedPackageService purchasedPackageService;

    @Mock
    private UserService userService;

    @Mock
    private WaitlistService waitlistService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void createBooking_shouldSucceed_whenConditionsMet() {
        Long userId = 1L;
        Long classId = 2L;
        Long packageId = 3L;

        String redisKey = "booking:lock:" + classId + ":" + userId;
        when(valueOperations.setIfAbsent(eq(redisKey), eq("locked"), any())).thenReturn(true);

        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setId(classId);
        classSchedule.setStartTime(LocalDateTime.now().plusDays(1));
        classSchedule.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        classSchedule.setRequiredCredits(1);
        classSchedule.setCountryCode("SG");
        classSchedule.setMaxCapacity(10);

        User user = new User();
        user.setId(userId);

        PurchasedPackage purchasedPackage = new PurchasedPackage();
        purchasedPackage.setId(packageId);

        when(bookingRepository.existsByUserIdAndBookedClassIdAndStatus(userId, classId, BookingStatus.CONFIRMED)).thenReturn(false);
        when(classScheduleService.getClassWithLock(classId)).thenReturn(classSchedule);
        when(userService.getUserById(userId)).thenReturn(user);
        when(bookingRepository.countByBookedClassIdAndStatus(classId, BookingStatus.CONFIRMED)).thenReturn(0L);
        when(bookingRepository.findUserBookingsBetweenDates(any(), any(), any())).thenReturn(List.of());
        when(purchasedPackageService.getPackageForBooking(userId, packageId, "SG")).thenReturn(purchasedPackage);
        doNothing().when(purchasedPackageService).deductCredits(packageId, 1);

        Booking savedBooking = new Booking();
        savedBooking.setId(10L);
        savedBooking.setUser(user);
        savedBooking.setBookedClass(classSchedule);
        savedBooking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        Booking result = bookingService.createBooking(userId, classId, packageId);

        assertNotNull(result);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());

        verify(redisTemplate).delete(redisKey);
    }

    @Test
    void createBooking_shouldThrowConflict_whenBookingExists() {
        when(valueOperations.setIfAbsent(anyString(), eq("locked"), any())).thenReturn(true);
        when(bookingRepository.existsByUserIdAndBookedClassIdAndStatus(anyLong(), anyLong(), any()))
                .thenReturn(true);

        assertThrows(ConflictException.class, () ->
                bookingService.createBooking(1L, 2L, 3L)
        );
    }

    @Test
    void createBooking_shouldThrowConcurrency_whenLockNotAcquired() {
        when(valueOperations.setIfAbsent(anyString(), eq("locked"), any())).thenReturn(false);

        assertThrows(ConcurrencyException.class, () ->
                bookingService.createBooking(1L, 2L, 3L)
        );
    }

    @Test
    void cancelBooking_shouldRefund_whenBeforeDeadline() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);

        ClassSchedule bookedClass = new ClassSchedule();
        bookedClass.setId(2L);
        bookedClass.setStartTime(LocalDateTime.now().plusDays(1));
        bookedClass.setRequiredCredits(2);
        bookedClass.setMaxCapacity(10);

        booking.setBookedClass(bookedClass);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.cancelBooking(eq(1L), any())).thenReturn(1);
        when(purchasedPackageService.getActivePackageForBooking(booking)).thenReturn(
                new PurchasedPackage() {{ setId(5L); }}
        );
        when(bookingRepository.countByBookedClassIdAndStatus(2L, BookingStatus.CONFIRMED)).thenReturn(10L);
        doNothing().when(waitlistService).processWaitlist(2L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking)); // refreshed

        Booking result = bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        verify(purchasedPackageService).refundCredits(5L, 2);
    }

    @Test
    void isClassFull_shouldReturnTrue_whenMaxReached() {
        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setId(1L);
        classSchedule.setMaxCapacity(2);

        when(classScheduleService.getClassWithLock(1L)).thenReturn(classSchedule);
        when(bookingRepository.countByBookedClassIdAndStatus(1L, BookingStatus.CONFIRMED)).thenReturn(2L);

        assertTrue(bookingService.isClassFull(1L));
    }

    @Test
    void hasOverlappingBookings_shouldReturnTrue_whenOverlapsExist() {
        Booking b = new Booking();
        b.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findUserBookingsBetweenDates(any(), any(), any())).thenReturn(List.of(b));

        boolean result = bookingService.hasOverlappingBookings(1L, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));

        assertTrue(result);
    }
}