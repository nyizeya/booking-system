package co.codigo.bookingsystem.domain.packageplan.service;

import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.repository.PackagePlanRepository;
import co.codigo.bookingsystem.web.dtos.requests.PackagePlanRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PackagePlanServiceTest {
    @Mock
    private PackagePlanRepository packagePlanRepository;

    @InjectMocks
    private PackagePlanService packagePlanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getActivePackagesByCountry_shouldReturnList() {
        String country = "SG";
        List<PackagePlan> plans = List.of(new PackagePlan());
        when(packagePlanRepository.findByCountryCodeAndActiveTrue(country)).thenReturn(plans);

        List<PackagePlan> result = packagePlanService.getActivePackagesByCountry(country);

        assertEquals(1, result.size());
        verify(packagePlanRepository).findByCountryCodeAndActiveTrue(country);
    }

    @Test
    void getPackageById_shouldReturnPackage() {
        PackagePlan plan = new PackagePlan();
        plan.setId(1L);

        when(packagePlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        PackagePlan result = packagePlanService.getPackageById(1L);

        assertEquals(1L, result.getId());
        verify(packagePlanRepository).findById(1L);
    }

    @Test
    void getPackageById_shouldThrowExceptionIfNotFound() {
        when(packagePlanRepository.findById(99L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () ->
                packagePlanService.getPackageById(99L)
        );

        assertTrue(ex.getMessage().contains("Package"));
    }

    @Test
    void createPackage_shouldThrowConflictIfExists() {
        PackagePlanRequest plan = new PackagePlanRequest();
        plan.setName("Gold");
        plan.setCountryCode("SG");

        when(packagePlanRepository.existsByNameAndCountryCode("Gold", "SG")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                packagePlanService.createPackage(plan)
        );

        verify(packagePlanRepository, never()).save(any());
    }

    @Test
    void createPackage_shouldSaveIfNotExists() {
        PackagePlan plan = new PackagePlan();
        plan.setName("Silver");
        plan.setCountryCode("MY");

        when(packagePlanRepository.existsByNameAndCountryCode("Silver", "MY")).thenReturn(false);
        when(packagePlanRepository.save(plan)).thenReturn(plan);

        PackagePlanRequest request = PackagePlanRequest.builder()
                .name(plan.getName())
                .countryCode(plan.getCountryCode())
                .build();
        PackagePlan saved = packagePlanService.createPackage(request);

        assertEquals(plan, saved);
        verify(packagePlanRepository).save(plan);
    }

    @Test
    void updatePackage_shouldCopyFieldsAndSave() {
        Long id = 1L;
        PackagePlan existing = new PackagePlan();
        existing.setId(id);
        existing.setName("Basic");

        PackagePlanRequest updates = new PackagePlanRequest();
        updates.setName("Updated Name");

        when(packagePlanRepository.findById(id)).thenReturn(Optional.of(existing));
        when(packagePlanRepository.save(any(PackagePlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PackagePlan result = packagePlanService.updatePackage(id, updates);

        assertEquals("Updated Name", result.getName());
        verify(packagePlanRepository).save(existing);
    }
}