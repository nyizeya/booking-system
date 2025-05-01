package co.codigo.bookingsystem.domain.purchasedpkg.repository;

import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchasedPackageRepository extends JpaRepository<PurchasedPackage, Long> {
    
    List<PurchasedPackage> findByUserId(Long userId);

    Optional<PurchasedPackage> findByUserIdAndPackagePlanId(Long userId, Long packageId);
    
    @Query("SELECT pp FROM PurchasedPackage pp WHERE pp.user.id = :userId " +
           "AND pp.expiryDate > :now AND pp.remainingCredits > 0")
    List<PurchasedPackage> findActivePackagesForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT pp FROM PurchasedPackage pp WHERE pp.user.id = :userId " +
           "AND pp.packagePlan.countryCode = :countryCode " +
           "AND pp.expiryDate > :now AND pp.remainingCredits > 0")
    List<PurchasedPackage> findActivePackagesForUserAndCountry(@Param("userId") Long userId, @Param("countryCode") String countryCode, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE PurchasedPackage pp SET pp.remainingCredits = pp.remainingCredits - :credits " +
           "WHERE pp.id = :userPackageId AND pp.remainingCredits >= :credits")
    int deductCredits(@Param("userPackageId") Long userPackageId, @Param("credits") Integer credits);
    
    @Modifying
    @Query("UPDATE PurchasedPackage up SET up.remainingCredits = up.remainingCredits + :credits " +
           "WHERE up.id = :userPackageId")
    int refundCredits(@Param("userPackageId") Long userPackageId, @Param("credits") Integer credits);

    List<PurchasedPackage> findByExpiryDateBeforeAndRemainingCreditsGreaterThan(
            @Param("expiryDate") LocalDateTime expiryDate,
            @Param("remainingCredits") Integer remainingCredits
    );
}