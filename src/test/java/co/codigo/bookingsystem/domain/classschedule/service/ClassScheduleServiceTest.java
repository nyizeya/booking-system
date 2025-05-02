package co.codigo.bookingsystem.domain.classschedule.service;

import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.classschedule.repository.ClassScheduleRepository;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.service.PackagePlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ClassScheduleServiceTest {

    @Mock
    private ClassScheduleRepository classRepository;

    @Mock
    private PackagePlanService packagePlanService;

    @InjectMocks
    private ClassScheduleService classService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUpcomingClassesByCountry_returnsList() {
        String countryCode = "SG";
        List<ClassSchedule> expected = List.of(new ClassSchedule());
        when(classRepository.findUpcomingClassesByCountry(eq(countryCode), any())).thenReturn(expected);

        List<ClassSchedule> result = classService.getUpcomingClassesByCountry(countryCode);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getClassWithLock_returnsClass() {
        ClassSchedule classSchedule = new ClassSchedule();
        when(classRepository.findById(1L)).thenReturn(Optional.of(classSchedule));

        ClassSchedule result = classService.getClassWithLock(1L);
        assertThat(result).isEqualTo(classSchedule);
    }

    @Test
    void getClassWithLock_throwsExceptionIfNotFound() {
        when(classRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> classService.getClassWithLock(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void createClass_throwsException_whenNoPackages() {
        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setCountryCode("MY");

        when(packagePlanService.getActivePackagesByCountry("MY"))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> classService.createClass(classSchedule))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void createClass_savesClass_whenPackagesExist() {
        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setCountryCode("MY");

        when(packagePlanService.getActivePackagesByCountry("MY"))
                .thenReturn(List.of(new PackagePlan()));
        when(classRepository.save(any())).thenReturn(classSchedule);

        ClassSchedule result = classService.createClass(classSchedule);
        assertThat(result).isEqualTo(classSchedule);
    }

    @Test
    void findAllEndedClasses_returnsList() {
        List<ClassSchedule> expected = List.of(new ClassSchedule());
        when(classRepository.findByEndTimeBefore(any())).thenReturn(expected);

        List<ClassSchedule> result = classService.findAllEndedClasses();
        assertThat(result).isEqualTo(expected);
    }

}