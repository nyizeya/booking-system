package co.codigo.bookingsystem.domain.purchasedpkg.service;

import co.codigo.bookingsystem.common.exceptions.InsufficientCreditsException;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.service.PackagePlanService;
import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import co.codigo.bookingsystem.domain.purchasedpkg.repository.PurchasedPackageRepository;
import co.codigo.bookingsystem.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchasedPackageServiceTest {
    @InjectMocks
    private PurchasedPackageService purchasedPackageService;

    @Mock
    private PurchasedPackageRepository purchasedPackageRepository;

    @Mock
    private PackagePlanService packagePlanService;

    @Mock
    private UserService userService;

    private final Long userId = 1L;
    private final Long packageId = 2L;
    private final String countryCode = "SG";

    @Test
    void getUserValidPackages_shouldReturnValidPackages() {
        List<PurchasedPackage> mockPackages = List.of(new PurchasedPackage());
        when(purchasedPackageRepository.findActivePackagesForUserAndCountry(eq(userId), eq(countryCode), any()))
                .thenReturn(mockPackages);

        List<PurchasedPackage> result = purchasedPackageService.getUserValidPackages(userId, countryCode);

        assertEquals(1, result.size());
        verify(purchasedPackageRepository).findActivePackagesForUserAndCountry(eq(userId), eq(countryCode), any());
    }

    @Test
    void deductCredits_shouldDeductSuccessfully() {
        PurchasedPackage purchasedPackage = new PurchasedPackage();
        purchasedPackage.setId(packageId);
        purchasedPackage.setRemainingCredits(10);

        when(purchasedPackageRepository.findById(packageId)).thenReturn(Optional.of(purchasedPackage));
        when(purchasedPackageRepository.save(any())).thenReturn(purchasedPackage);

        purchasedPackageService.deductCredits(packageId, 5);

        assertEquals(5, purchasedPackage.getRemainingCredits());
        verify(purchasedPackageRepository).save(purchasedPackage);
    }

    @Test
    void deductCredits_shouldThrowIfInsufficientCredits() {
        PurchasedPackage purchasedPackage = new PurchasedPackage();
        purchasedPackage.setId(packageId);
        purchasedPackage.setRemainingCredits(3);

        when(purchasedPackageRepository.findById(packageId)).thenReturn(Optional.of(purchasedPackage));

        assertThrows(InsufficientCreditsException.class, () ->
                purchasedPackageService.deductCredits(packageId, 5)
        );
    }

    @Test
    void refundCredits_shouldIncreaseCredits() {
        PurchasedPackage purchasedPackage = new PurchasedPackage();
        purchasedPackage.setId(packageId);
        purchasedPackage.setRemainingCredits(5);

        when(purchasedPackageRepository.findById(packageId)).thenReturn(Optional.of(purchasedPackage));
        when(purchasedPackageRepository.save(any())).thenReturn(purchasedPackage);

        purchasedPackageService.refundCredits(packageId, 3);

        assertEquals(8, purchasedPackage.getRemainingCredits());
        verify(purchasedPackageRepository).save(purchasedPackage);
    }

    @Test
    void getPackageForBooking_shouldReturnValidPackage() {
        PackagePlan plan = new PackagePlan();
        plan.setCountryCode(countryCode);
        PurchasedPackage pkg = new PurchasedPackage();
        pkg.setPackagePlan(plan);
        pkg.setRemainingCredits(5);
        pkg.setExpiryDate(LocalDateTime.now().plusDays(1));

        when(purchasedPackageRepository.findByUserIdAndPackagePlanId(userId, packageId))
                .thenReturn(Optional.of(pkg));

        PurchasedPackage result = purchasedPackageService.getPackageForBooking(userId, packageId, countryCode);

        assertNotNull(result);
        verify(purchasedPackageRepository).findByUserIdAndPackagePlanId(userId, packageId);
    }
}