package co.codigo.bookingsystem.domain.purchasedpkg.service;

import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.common.exceptions.InsufficientCreditsException;
import co.codigo.bookingsystem.common.utils.CommonUtils;
import co.codigo.bookingsystem.domain.booking.entity.Booking;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.service.PackagePlanService;
import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import co.codigo.bookingsystem.domain.purchasedpkg.repository.PurchasedPackageRepository;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchasedPackageService {

    private final PurchasedPackageRepository purchasedPackageRepository;
    private final PackagePlanService packagePlanService;
    private final UserService userService;

    public List<PurchasedPackage> getUserValidPackages(Long userId, String countryCode) {
        return purchasedPackageRepository.findActivePackagesForUserAndCountry(
            userId,
            countryCode,
            LocalDateTime.now()
        );
    }

    public PurchasedPackage getActivePackageForBooking(Booking booking) {
        List<PurchasedPackage> validPackages = purchasedPackageRepository
                .findActivePackagesForUserAndCountry(
                        booking.getUser().getId(),
                        booking.getBookedClass().getCountryCode(),
                        LocalDateTime.now()
                );

        return validPackages.stream()
                .min(Comparator.comparing(PurchasedPackage::getExpiryDate))
                .orElseThrow(() -> new BusinessRuleException("No active package found for this booking"));
    }

    @Transactional
    public PurchasedPackage purchasePackage(Long userId, Long packageId, String paymentRef) {
        PackagePlan packagePlan = packagePlanService.getPackageById(packageId);
        User user = userService.getUserById(userId);

        PurchasedPackage purchasedPackage = new PurchasedPackage();
        purchasedPackage.setUser(user);
        purchasedPackage.setPackagePlan(packagePlan);
        purchasedPackage.setRemainingCredits(packagePlan.getCredits());
        purchasedPackage.setExpiryDate(
            LocalDateTime.now().plusDays(packagePlan.getExpiryDays())
        );
        purchasedPackage.setPaymentReference(paymentRef);

        return purchasedPackageRepository.save(purchasedPackage);
    }

    public PurchasedPackage getPackageForBooking(Long userId, Long packageId, String countryCode) {
        PurchasedPackage purchasedPackage = purchasedPackageRepository.findByUserIdAndPackagePlanId(userId, packageId)
                .orElseThrow(() -> new EntityNotFoundException("User package with user Id [%d] and pkg id [%d] not found".formatted(userId, packageId)));

        if (!purchasedPackage.getPackagePlan().getCountryCode().equals(countryCode)) {
            throw new BusinessRuleException(String.format("Error your country: %s, package country: %s".formatted(countryCode, purchasedPackage.getPackagePlan().getCountryCode())));
        }

        // Check package is still valid
        if (purchasedPackage.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Package has expired");
        }

        // Check has sufficient credits
        if (purchasedPackage.getRemainingCredits() <= 0) {
            throw new BusinessRuleException("Package has no remaining credits");
        }

        return purchasedPackage;
    }

    @Transactional
    public void deductCredits(Long packageId, int credits) {
        PurchasedPackage purchasedPackage = purchasedPackageRepository.findById(packageId)
            .orElseThrow(() -> CommonUtils.createEntityNotFoundException("Purchased package", "id", packageId));

        if (purchasedPackage.getRemainingCredits() < credits) {
            throw new InsufficientCreditsException("Not enough credits");
        }

        purchasedPackage.setRemainingCredits(
            purchasedPackage.getRemainingCredits() - credits
        );

        purchasedPackageRepository.save(purchasedPackage);
    }

    @Transactional
    public void refundCredits(Long packageId, int credits) {
        PurchasedPackage purchasedPackage = purchasedPackageRepository.findById(packageId)
            .orElseThrow(() -> CommonUtils.createEntityNotFoundException("Purchased package", "id", packageId));

        purchasedPackage.setRemainingCredits(
            purchasedPackage.getRemainingCredits() + credits
        );

        purchasedPackageRepository.save(purchasedPackage);
    }
}