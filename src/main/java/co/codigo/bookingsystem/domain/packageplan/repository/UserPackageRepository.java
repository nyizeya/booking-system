package co.codigo.bookingsystem.domain.packageplan.repository;

import co.codigo.bookingsystem.domain.packageplan.entity.UserPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPackageRepository extends JpaRepository<UserPackage, Long> {
    List<UserPackage> findByUserId(Long userId);

    Optional<UserPackage> findByUserIdAndPackagePlanId(Long userId, Long packagePlanId);
}
