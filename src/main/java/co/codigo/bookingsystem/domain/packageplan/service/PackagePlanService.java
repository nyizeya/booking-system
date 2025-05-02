package co.codigo.bookingsystem.domain.packageplan.service;

import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.common.utils.CommonUtils;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.repository.PackagePlanRepository;
import co.codigo.bookingsystem.web.dtos.requests.PackagePlanRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PackagePlanService {

    private final PackagePlanRepository packagePlanRepository;

    public List<PackagePlan> getActivePackagesByCountry(String countryCode) {
        return packagePlanRepository.findByCountryCodeAndActiveTrue(countryCode);
    }

    public PackagePlan getPackageById(Long id) {
        return packagePlanRepository.findById(id)
            .orElseThrow(() -> CommonUtils.createEntityNotFoundException("Package", "id", id));
    }

    @Transactional
    public PackagePlan createPackage(PackagePlanRequest request) {
        if (packagePlanRepository.existsByNameAndCountryCode(
            request.getName(),
            request.getCountryCode())) {
            throw new ConflictException("Package already exists for this country");
        }

        PackagePlan packagePlan = PackagePlan.builder()
                .name(request.getName())
                .countryCode(request.getCountryCode())
                .price(request.getPrice())
                .credits(request.getCreditCount())
                .expiryDays(request.getExpiryDays())
                .active(request.getActive())
                .build();

        return packagePlanRepository.save(packagePlan);
    }

    @Transactional
    public PackagePlan updatePackage(Long id, PackagePlanRequest updates) {
        PackagePlan existing = getPackageById(id);
        existing.setName(updates.getName());
        existing.setCountryCode(updates.getCountryCode());
        existing.setPrice(updates.getPrice());
        existing.setCredits(updates.getCreditCount());
        existing.setExpiryDays(updates.getExpiryDays());
        existing.setActive(updates.getActive());
        return packagePlanRepository.save(existing);
    }
}