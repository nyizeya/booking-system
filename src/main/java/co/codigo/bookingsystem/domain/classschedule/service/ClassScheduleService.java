package co.codigo.bookingsystem.domain.classschedule.service;

import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.common.utils.CommonUtils;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.classschedule.repository.ClassScheduleRepository;
import co.codigo.bookingsystem.domain.packageplan.service.PackagePlanService;
import co.codigo.bookingsystem.web.dtos.requests.CreateClassScheduleRequest;
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

    public List<ClassSchedule> getAllActiveClasses() {
        return classRepository.findAllActiveClassSchedules();
    }

    public List<ClassSchedule> getClassSchedulesByCountryCode(String countryCode) {
        return classRepository.findByCountryCode(countryCode);
    }

    public ClassSchedule getClassScheduleById(Long classId) {
        return classRepository.findById(classId)
            .orElseThrow(() -> CommonUtils.createEntityNotFoundException("Class schedule", "id", classId));
    }

    @Transactional
    public ClassSchedule createClass(CreateClassScheduleRequest request) {
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Start time cannot be past time");
        }

        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setName(request.getName());
        classSchedule.setDescription(request.getDescription());
        classSchedule.setCountryCode(request.getCountryCode());
        classSchedule.setRequiredCredits(request.getRequiredCredits());
        classSchedule.setMaxCapacity(request.getMaxCapacity());
        classSchedule.setStartTime(request.getStartTime());
        classSchedule.setEndTime(request.getEndTime());

        return classRepository.save(classSchedule);
    }
}