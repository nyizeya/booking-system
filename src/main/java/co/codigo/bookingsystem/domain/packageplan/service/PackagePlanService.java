package co.codigo.bookingsystem.domain.packageplan.service;

import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.common.utils.CommonUtils;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.repository.PackagePlanRepository;
import co.codigo.bookingsystem.web.dtos.requests.CreatePackagePlanRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackagePlanService {

    private final PackagePlanRepository packagePlanRepository;

    public List<PackagePlan> getAllActivePackages() {
        return packagePlanRepository.findAllActivePackages();
    }

    public List<PackagePlan> findAllAvailablePackagesForUser(Long userId) {
        return packagePlanRepository.findActivePackagesUserHasNotBought(userId);
    }

    public List<PackagePlan> findAllPurchasedPackagesForUser(Long userId) {
        return packagePlanRepository.findActivePackagesUserHasBought(userId);
    }

    public List<PackagePlan> getActivePackagesByCountry(String countryCode) {
        return packagePlanRepository.findByCountryCode(countryCode);
    }

    public PackagePlan getPackageById(Long id) {
        return packagePlanRepository.findById(id)
            .orElseThrow(() -> CommonUtils.createEntityNotFoundException("Package", "id", id));
    }

    @Transactional
    public PackagePlan createPackage(CreatePackagePlanRequest request) {
        if (packagePlanRepository.existsByNameAndCountryCode(request.getName(), request.getCountryCode()))
            throw new ConflictException("Package already exists for this country");

        if (request.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new BusinessRuleException("Package expiry date cannot be past.");

        PackagePlan packagePlan = PackagePlan.builder()
                .name(request.getName())
                .countryCode(request.getCountryCode())
                .price(request.getPrice())
                .credits(request.getCreditCount())
                .expiryDate(request.getExpiryDate())
                .build();

        return packagePlanRepository.save(packagePlan);
    }
}