package co.codigo.bookingsystem.domain.packageplan.service;

import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.common.utils.CommonUtils;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.repository.PackagePlanRepository;
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
    public PackagePlan createPackage(PackagePlan packagePlan) {
        if (packagePlanRepository.existsByNameAndCountryCode(
            packagePlan.getName(), 
            packagePlan.getCountryCode())) {
            throw new ConflictException("Package already exists for this country");
        }
        return packagePlanRepository.save(packagePlan);
    }

    @Transactional
    public PackagePlan updatePackage(Long id, PackagePlan updates) {
        PackagePlan existing = getPackageById(id);
        BeanUtils.copyProperties(updates, existing, "id", "createdAt", "createdBy");
        return packagePlanRepository.save(existing);
    }
}