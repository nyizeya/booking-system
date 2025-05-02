package co.codigo.bookingsystem.domain.purchasedpkg.repository;

import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchasedPackageRepository extends JpaRepository<PurchasedPackage, Long> {
    
    List<PurchasedPackage> findByUserId(Long userId);

    Optional<PurchasedPackage> findByUserIdAndPackagePlanId(Long userId, Long packageId);
    
    @Query("SELECT pp FROM PurchasedPackage pp WHERE pp.user.id = :userId AND pp.expiryDate > :now")
    List<PurchasedPackage> findNonExpiredPurchasedPackagesForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT pp FROM PurchasedPackage pp WHERE pp.user.id = :userId AND pp.expiryDate > :now AND pp.remainingCredits > 0")
    List<PurchasedPackage> findUsablePurchasedPackages(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}