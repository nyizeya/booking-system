package co.codigo.bookingsystem.domain.packageplan.repository;

import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PackagePlanRepository extends JpaRepository<PackagePlan, Long> {

    List<PackagePlan> findByCountryCode(String countryCode);

    @Query("SELECT pp FROM PackagePlan pp WHERE pp.expiryDate > CURRENT_TIMESTAMP")
    List<PackagePlan> findAllActivePackages();

    boolean existsByNameAndCountryCode(String name, String countryCode);

    @Query("""
        SELECT p FROM PackagePlan p
        WHERE p.expiryDate > CURRENT_TIMESTAMP
        AND NOT EXISTS (
            SELECT 1 FROM UserPackage up
            WHERE up.user.id = :userId AND up.packagePlan.id = p.id
          )
        """)
    List<PackagePlan> findActivePackagesUserHasNotBought(@Param("userId") Long userId);


    @Query("""
        SELECT p FROM PackagePlan p
        WHERE p.expiryDate > CURRENT_TIMESTAMP
        AND EXISTS (
            SELECT 1 FROM UserPackage up
            WHERE up.user.id = :userId AND up.packagePlan.id = p.id
          )
        """)
    List<PackagePlan> findActivePackagesUserHasBought(@Param("userId") Long userId);

}