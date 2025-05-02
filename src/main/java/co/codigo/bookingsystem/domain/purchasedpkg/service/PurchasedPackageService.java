package co.codigo.bookingsystem.domain.purchasedpkg.service;

import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.common.exceptions.InsufficientCreditsException;
import co.codigo.bookingsystem.common.utils.CommonUtils;
import co.codigo.bookingsystem.domain.booking.entity.Booking;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.service.PackagePlanService;
import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import co.codigo.bookingsystem.domain.purchasedpkg.repository.PurchasedPackageRepository;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchasedPackageService {

    private final PurchasedPackageRepository purchasedPackageRepository;
    private final PackagePlanService packagePlanService;
    private final UserRepository userRepository;

    public List<PurchasedPackage> getAllPurchasedPackagesForUser(Long userId) {
        return purchasedPackageRepository.findByUserId(userId);
    }

    @Transactional
    public PurchasedPackage purchasePackage(User user, Long packageId) {
        PackagePlan packagePlan = packagePlanService.getPackageById(packageId);

        if (user.getBalance().subtract(packagePlan.getPrice()).compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("User don't have enough balance to purchase package plan.");
        }

        user.setBalance(user.getBalance().subtract(packagePlan.getPrice()));
        PurchasedPackage purchasedPackage = new PurchasedPackage();
        purchasedPackage.setUser(user);
        purchasedPackage.setPackagePlan(packagePlan);
        purchasedPackage.setRemainingCredits(packagePlan.getCredits());
        purchasedPackage.setExpiryDate(packagePlan.getExpiryDate());
        userRepository.save(user);
        return purchasedPackageRepository.save(purchasedPackage);
    }

    public PurchasedPackage getPackageForBooking(Long userId, Long packageId, String classCountryCode) {
        PurchasedPackage purchasedPackage = purchasedPackageRepository.findByUserIdAndPackagePlanId(userId, packageId)
                .orElseThrow(() -> new EntityNotFoundException("User package with user Id [%d] and pkg id [%d] not found".formatted(userId, packageId)));

        if (purchasedPackage.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Package has expired");
        }

        if (purchasedPackage.getPackagePlan().getCountryCode().equals(classCountryCode)) {
            throw new BusinessRuleException("Package and class must be in the same country");
        }

        return purchasedPackage;
    }

    @Transactional
    public void deductCredits(PurchasedPackage purchasedPackage, int requiredCredits) {
        if (purchasedPackage.getRemainingCredits() < requiredCredits) {
            throw new InsufficientCreditsException("Not enough requiredCredits");
        }

        purchasedPackage.setRemainingCredits(purchasedPackage.getRemainingCredits() - requiredCredits);
        purchasedPackageRepository.save(purchasedPackage);
    }

    @Transactional
    public void refundCredits(PurchasedPackage purchasedPackage, int credits) {
        purchasedPackage.setRemainingCredits(purchasedPackage.getRemainingCredits() + credits);
        purchasedPackageRepository.save(purchasedPackage);
    }
}