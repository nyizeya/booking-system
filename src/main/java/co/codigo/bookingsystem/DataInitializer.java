package co.codigo.bookingsystem;

import co.codigo.bookingsystem.domain.booking.entity.Booking;
import co.codigo.bookingsystem.domain.booking.repository.BookingRepository;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.classschedule.service.ClassScheduleService;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.repository.PackagePlanRepository;
import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import co.codigo.bookingsystem.domain.purchasedpkg.service.PurchasedPackageService;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.service.UserService;
import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import co.codigo.bookingsystem.domain.waitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PackagePlanRepository packagePlanRepository;
    private final UserService userService;
    private final PurchasedPackageService purchasedPackageService;
    private final ClassScheduleService classScheduleService;
    private final BookingRepository bookingRepository;
    private final WaitlistRepository waitlistRepository;

    @Override
    public void run(String... args) throws Exception {
        User user1 = new User("user1@example.com", "User One", "password123");
        User user2 = new User("user2@example.com", "User Two", "password123");
        userService.createNewUser(user1);
        userService.createNewUser(user2);

        PackagePlan packagePlan1 = new PackagePlan("Standard Plan", "US", 100, new BigDecimal(100), LocalDateTime.now().plusDays(30));
        PackagePlan packagePlan2 = new PackagePlan("Premium Plan", "US", 200, new BigDecimal(100), LocalDateTime.now().plusDays(30));
        packagePlanRepository.save(packagePlan1);
        packagePlanRepository.save(packagePlan2);

        PurchasedPackage purchasedPackage1 = purchasedPackageService.purchasePackage(user1, packagePlan1.getId());
        PurchasedPackage purchasedPackage2 = purchasedPackageService.purchasePackage(user2, packagePlan2.getId());

        ClassSchedule class1 = new ClassSchedule("Yoga Class", "US", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1), 10, 5, 20);
        ClassSchedule class2 = new ClassSchedule("Pilates Class", "US", LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(1), 10, 6, 30);
        classScheduleService.createClass(class1);
        classScheduleService.createClass(class2);

        Waitlist waitlist1 = new Waitlist(user1, class1, 1, LocalDateTime.now());
        Waitlist waitlist2 = new Waitlist(user2, class2, 2, LocalDateTime.now());
        waitlistRepository.save(waitlist1);
        waitlistRepository.save(waitlist2);

        Booking booking1 = new Booking(user1, class1, LocalDateTime.now());
        Booking booking2 = new Booking(user2, class2, LocalDateTime.now());
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);

        System.out.println("Initial data has been loaded successfully!");
    }
}
