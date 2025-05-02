package co.codigo.bookingsystem.domain.packageplan.repository;

import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PackagePlanRepository extends JpaRepository<PackagePlan, Long> {

    boolean existsByNameAndCountryCode(String name, String countryCode);

    List<PackagePlan> findByCountryCode(String countryCode);


    // find packages in the same countryCode that hasn't expired and user hasn't bought yet
    @Query("SELECT p FROM PackagePlan p WHERE p.countryCode = :countryCode AND p.expiryDate > :now " +
           "AND NOT EXISTS (SELECT pp FROM PurchasedPackage pp WHERE pp.packagePlan = p AND pp.user.id = :userId)")
    List<PackagePlan> findAvailablePackagesForUser(
            @Param("countryCode") String countryCode,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );
}