package co.codigo.bookingsystem.domain.booking.service;

import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.classschedule.service.ClassScheduleService;
import co.codigo.bookingsystem.domain.booking.entity.Booking;
import co.codigo.bookingsystem.domain.booking.repository.BookingRepository;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.service.PackagePlanService;
import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import co.codigo.bookingsystem.domain.purchasedpkg.service.PurchasedPackageService;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.web.dtos.requests.BookingRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final PackagePlanService packagePlanService;
    private final ClassScheduleService classScheduleService;
    private final PurchasedPackageService purchasedPackageService;

    public Booking findBookedClass(Long userId, Long bookedClassId) {
        return bookingRepository.findByUserIdAndBookedClassId(userId, bookedClassId).orElseThrow(()
                -> new EntityNotFoundException("Booking not found for userId [%d] and classId [%d]".formatted(userId, bookedClassId)));
    }

    @Transactional
    public Booking createBooking(User user, BookingRequest bookingRequest) {
        Long classId = bookingRequest.getClassId();
        Long packageId = bookingRequest.getPackageId();
        ClassSchedule classSchedule = classScheduleService.getClassScheduleId(classId);
        PackagePlan packagePlan = packagePlanService.getPackageById(packageId);

        if (packagePlan.getCountryCode().equals(classSchedule.getCountryCode())) {
            throw new ConflictException("Package and class must be in the same country");
        }

        if (bookingRepository.existsByIdAndUserId(user.getId(), classId)) {
            throw new ConflictException("You already have an active booking for this class");
        }

        if (isClassFull(classSchedule)) {
            throw new ConflictException("Class is fully booked");
        }

        PurchasedPackage purchasedPackage = purchasedPackageService.getPackageForBooking(user.getId(), packageId, classSchedule.getCountryCode());
        purchasedPackageService.deductCredits(purchasedPackage, classSchedule.getRequiredCredits());

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBookedClass(classSchedule);
        booking.setPurchasedPackage(purchasedPackage);
        booking.setBookedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    @Transactional
    public void cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found for userId [%d] and bookingId [%d]".formatted(userId, bookingId)));

        LocalDateTime cancellationDeadline = booking.getBookedClass().getStartTime().minusHours(4);
        boolean isRefundable = LocalDateTime.now().isBefore(cancellationDeadline);

        if (isRefundable) {
            purchasedPackageService.refundCredits(
                    booking.getPurchasedPackage(),
                    booking.getBookedClass().getRequiredCredits()
            );
        }

        deleteBooking(bookingId);
    }

    public void deleteBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Transactional(readOnly = true)
    public boolean isClassFull(ClassSchedule classSchedule) {
        long bookedCount = bookingRepository.countByBookedClassId(classSchedule.getId());
        return bookedCount >= classSchedule.getMaxCapacity();
    }
}