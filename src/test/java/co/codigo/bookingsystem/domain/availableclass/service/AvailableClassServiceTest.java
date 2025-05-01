package co.codigo.bookingsystem.domain.availableclass.service;

import co.codigo.bookingsystem.common.exceptions.BusinessRuleException;
import co.codigo.bookingsystem.domain.availableclass.entity.AvailableClass;
import co.codigo.bookingsystem.domain.availableclass.repository.AvailableClassRepository;
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

class AvailableClassServiceTest {

    @Mock
    private AvailableClassRepository classRepository;

    @Mock
    private PackagePlanService packagePlanService;

    @InjectMocks
    private AvailableClassService classService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUpcomingClassesByCountry_returnsList() {
        String countryCode = "SG";
        List<AvailableClass> expected = List.of(new AvailableClass());
        when(classRepository.findUpcomingClassesByCountry(eq(countryCode), any())).thenReturn(expected);

        List<AvailableClass> result = classService.getUpcomingClassesByCountry(countryCode);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getClassWithLock_returnsClass() {
        AvailableClass availableClass = new AvailableClass();
        when(classRepository.findById(1L)).thenReturn(Optional.of(availableClass));

        AvailableClass result = classService.getClassWithLock(1L);
        assertThat(result).isEqualTo(availableClass);
    }

    @Test
    void getClassWithLock_throwsExceptionIfNotFound() {
        when(classRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> classService.getClassWithLock(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void createClass_throwsException_whenNoPackages() {
        AvailableClass availableClass = new AvailableClass();
        availableClass.setCountryCode("MY");

        when(packagePlanService.getActivePackagesByCountry("MY"))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> classService.createClass(availableClass))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void createClass_savesClass_whenPackagesExist() {
        AvailableClass availableClass = new AvailableClass();
        availableClass.setCountryCode("MY");

        when(packagePlanService.getActivePackagesByCountry("MY"))
                .thenReturn(List.of(new PackagePlan()));
        when(classRepository.save(any())).thenReturn(availableClass);

        AvailableClass result = classService.createClass(availableClass);
        assertThat(result).isEqualTo(availableClass);
    }

    @Test
    void findAllEndedClasses_returnsList() {
        List<AvailableClass> expected = List.of(new AvailableClass());
        when(classRepository.findByEndTimeBefore(any())).thenReturn(expected);

        List<AvailableClass> result = classService.findAllEndedClasses();
        assertThat(result).isEqualTo(expected);
    }

}