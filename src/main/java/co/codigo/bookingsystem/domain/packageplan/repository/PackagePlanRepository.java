package co.codigo.bookingsystem.domain.packageplan.repository;

import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PackagePlanRepository extends JpaRepository<PackagePlan, Long> {

    boolean existsByNameAndCountryCode(String name, String countryCode);

    List<PackagePlan> findByCountryCodeAndActiveTrue(String countryCode);
    
    @Query("SELECT p FROM PackagePlan p WHERE p.active = true AND p.countryCode = :countryCode " +
           "AND NOT EXISTS (SELECT pp FROM PurchasedPackage pp WHERE pp.packagePlan = p AND pp.user.id = :userId)")
    List<Package> findAvailablePackagesForUser(@Param("countryCode") String countryCode, @Param("userId") Long userId);
    
    @Query("SELECT p FROM PackagePlan p WHERE p.active = true")
    List<Package> findAllActive();
}