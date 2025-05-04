package co.codigo.bookingsystem.domain.waitlist.service;

import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.domain.booking.repository.BookingRepository;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.classschedule.service.ClassScheduleService;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.service.PackagePlanService;
import co.codigo.bookingsystem.domain.packageplan.service.UserPackageService;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.service.UserService;
import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import co.codigo.bookingsystem.domain.waitlist.repository.WaitlistRepository;
import co.codigo.bookingsystem.web.dtos.requests.CreateWaitlistRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final WaitlistRepository waitlistRepository;
    private final PackagePlanService packagePlanService;
    private final UserPackageService userPackageService;
    private final ClassScheduleService classScheduleService;

    public Optional<Waitlist> findTheOldestEntry() {
        return waitlistRepository.findTopByOrderByCreatedAtAsc();
    }

    @Transactional
    public Waitlist addToWaitlist(Long userId, CreateWaitlistRequest request) {
        User user = userService.getUserById(userId);
        ClassSchedule classSchedule = classScheduleService.getClassScheduleById(request.getClassId());
        PackagePlan packagePlan = packagePlanService.getPackageById(request.getPackageId());

        if (!isClassFull(classSchedule)) {
            throw new ConflictException("Class is not full. You can just attend it without adding to waitlist.");
        }

        log.info("Adding userId [{}] to waitlist for classId [{}]", user.getId(), classSchedule.getId());

        if (!packagePlan.getCountryCode().equals(classSchedule.getCountryCode()))
            throw new ConflictException("Country Code of package and class schedule do not match");

        if (waitlistRepository.existsByUserIdAndWaitingClassId(user.getId(), classSchedule.getId())) {
            throw new ConflictException("User already in waitlist");
        }

        Waitlist waitlist = new Waitlist(user, classSchedule, packagePlan);
        userPackageService.deductCredits(userId, packagePlan.getId(), classSchedule.getRequiredCredits());
        return waitlistRepository.save(waitlist);
    }

    @Transactional
    public void removeFromWaitlist(Waitlist waitlist) {
        log.info("Removing userId [{}] from waitlist for classId [{}]", waitlist.getUser().getId(), waitlist.getWaitingClass().getId());
        waitlistRepository.delete(waitlist);
    }

    @Scheduled(cron = "0 0/30 * * * *")
    @Transactional
    public void refundExpiredWaitlists() {
        log.info("Running refunded waitlist schedule");
        List<Waitlist> expiredWaitlists = waitlistRepository.findExpiredWaitlist();

        for (Waitlist waitlist : expiredWaitlists) {
            try {
                log.info("Auto-removing expired waitlistId [{}] for classId [{}]", waitlist.getId(), waitlist.getWaitingClass().getId());
                userPackageService.refundCredits(waitlist.getUser().getId(), waitlist.getPackagePlan().getId(), waitlist.getWaitingClass().getRequiredCredits());
                waitlistRepository.delete(waitlist);
            } catch (Exception e) {
                log.error("Error processing expired waitlist {}: {}", waitlist.getId(), e.getMessage());
                throw new ConflictException("Error processing expired waitlist %d: %s".formatted(waitlist.getId(), e.getMessage()));
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean isOnWaitlist(Long userId, Long classId) {
        return waitlistRepository.existsByUserIdAndWaitingClassId(userId, classId);
    }

    @Transactional(readOnly = true)
    public boolean isClassFull(ClassSchedule classSchedule) {
        long bookedCount = bookingRepository.countByBookedClassId(classSchedule.getId());
        return bookedCount >= classSchedule.getMaxCapacity();
    }
}