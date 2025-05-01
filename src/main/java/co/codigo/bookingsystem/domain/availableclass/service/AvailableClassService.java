package co.codigo.bookingsystem.domain.availableclass.service;

import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.common.utils.CommonUtils;
import co.codigo.bookingsystem.domain.availableclass.entity.AvailableClass;
import co.codigo.bookingsystem.domain.availableclass.repository.AvailableClassRepository;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.service.PackagePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailableClassService {

    private final AvailableClassRepository classRepository;
    private final PackagePlanService packagePlanService;

    public List<AvailableClass> getUpcomingClassesByCountry(String countryCode) {
        return classRepository.findUpcomingClassesByCountry(
            countryCode, 
            LocalDateTime.now()
        );
    }

    public AvailableClass getClassWithLock(Long classId) {
        return classRepository.findById(classId)
            .orElseThrow(() -> CommonUtils.createEntityNotFoundException("Available class", "id", classId));
    }

    @Transactional
    public AvailableClass createClass(AvailableClass availableClass) {
        List<PackagePlan> packages = packagePlanService.getActivePackagesByCountry(
                availableClass.getCountryCode()
        );
        if (packages.isEmpty()) {
            throw new BusinessRuleException(
                    "Cannot create class for country " + availableClass.getCountryCode() +
                            ": No active packages available for booking"
            );
        }
        return classRepository.save(availableClass);
    }

    public List<AvailableClass> findAllEndedClasses() {
        return classRepository.findByEndTimeBefore(LocalDateTime.now());
    }
}