package co.codigo.bookingsystem.domain.packageplan.service;

import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.common.exceptions.InsufficientCreditsException;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.entity.UserPackage;
import co.codigo.bookingsystem.domain.packageplan.entity.UserPackageId;
import co.codigo.bookingsystem.domain.packageplan.repository.UserPackageRepository;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPackageService {

    private final UserService userService;
    private final PackagePlanService packagePlanService;
    private final UserPackageRepository userPackageRepository;

    public List<UserPackage> findAllPurchasedPackages(Long userId) {
        return userPackageRepository.findByUserId(userId);
    }

    public UserPackage findByUserIdAndPackageId(Long userId, Long packageId) {
        return userPackageRepository.findByUserIdAndPackagePlanId(userId, packageId).orElseThrow(()
                -> new EntityNotFoundException("User package with userId [%d] and packageId [%d] not found".formatted(userId, packageId)));
    }

    @Transactional
    public void purchasePackage(Long userId, Long packageId) {
        User user = userService.getUserById(userId);
        PackagePlan packagePlan = packagePlanService.getPackageById(packageId);

        if (userPackageRepository.findByUserIdAndPackagePlanId(userId, packageId).isPresent()) {
            throw new ConflictException("User [%s] already bought the package [%s]".formatted(user.getUsername(), packagePlan.getName()));
        }

        UserPackageId userPackageId = new UserPackageId(user.getId(), packageId);
        UserPackage userPackage = new UserPackage(userPackageId, user, packagePlan, packagePlan.getCredits());
        userPackageRepository.save(userPackage);
        chargeUser(user, packagePlan.getPrice());
        userService.updateUser(user);
    }

    @Transactional
    public void deductCredits(Long userId, Long packageId, Integer credits) {
        UserPackage userPackage = findByUserIdAndPackageId(userId, packageId);

        log.info("Deducting credits {} from package {}",  credits, userPackage.getPackagePlan().getName());

        if (userPackage.getPackagePlan().getCredits() < credits) {
            throw new InsufficientCreditsException("Insufficient credits");
        }

        userPackage.setRemainingCredits(userPackage.getRemainingCredits() - credits);
        userPackageRepository.save(userPackage);
    }

    @Transactional
    public void refundCredits(Long userId, Long packageId, Integer credits) {
        UserPackage userPackage = findByUserIdAndPackageId(userId, packageId);

        log.info("Refunding {} credits back to package {} for userId {}", credits, userPackage.getPackagePlan().getName(), userId);
        userPackage.setRemainingCredits(userPackage.getRemainingCredits() + credits);
        userPackageRepository.save(userPackage);
    }

    public boolean chargeUser(User user, BigDecimal price) {
        user.setBalance(user.getBalance().subtract(price));
        return true;
    }


}
