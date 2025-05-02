package co.codigo.bookingsystem.domain.classschedule.service;

import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.common.utils.CommonUtils;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.classschedule.repository.ClassScheduleRepository;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.service.PackagePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassScheduleService {

    private final ClassScheduleRepository classRepository;
    private final PackagePlanService packagePlanService;

    public List<ClassSchedule> getAllClasses() {
        return classRepository.findAll();
    }

    public List<ClassSchedule> getClassSchedulesByCountryCode(String countryCode) {
        return classRepository.findByCountryCode(countryCode);
    }

    public ClassSchedule getClassScheduleId(Long classId) {
        return classRepository.findById(classId)
            .orElseThrow(() -> CommonUtils.createEntityNotFoundException("Class schedule", "id", classId));
    }

    @Transactional
    public ClassSchedule createClass(ClassSchedule classSchedule) {
        List<PackagePlan> packages = packagePlanService.getActivePackagesByCountry(
                classSchedule.getCountryCode()
        );

        if (packages.isEmpty()) {
            throw new BusinessRuleException(
                    "Cannot create class for country " + classSchedule.getCountryCode() +
                            ": No active packages available for booking"
            );
        }

        return classRepository.save(classSchedule);
    }
}