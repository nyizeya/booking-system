package co.codigo.bookingsystem.web.resources;

import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.service.PackagePlanService;
import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import co.codigo.bookingsystem.domain.purchasedpkg.service.PurchasedPackageService;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.security.service.UserDetailsImpl;
import co.codigo.bookingsystem.web.dtos.mappers.PackagePlanMapper;
import co.codigo.bookingsystem.web.dtos.mappers.PurchasedPackageMapper;
import co.codigo.bookingsystem.web.dtos.response.PackagePlanDto;
import co.codigo.bookingsystem.web.dtos.response.PurchasedPackageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static co.codigo.bookingsystem.common.constants.UrlConstant.PACKAGE_PLAN_URL;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PACKAGE_PLAN_URL)
public class PackagePlanResource {

    private final PackagePlanMapper packagePlanMapper;
    private final PackagePlanService packagePlanService;
    private final PurchasedPackageMapper purchasedPackageMapper;
    private final PurchasedPackageService purchasedPackageService;

    @GetMapping
    public List<PackagePlanDto> getActivePackagesForUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = ((UserDetailsImpl) userDetails).getUser();
        List<PackagePlan> packagePlans = packagePlanService.findAllAvailablePackagesForUser(user.getId());
        return packagePlanMapper.toDTOList(packagePlans);
    }

    @GetMapping("/purchased")
    public List<PurchasedPackageDto> getPurchasedPackagesForUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsImpl userDetail = (UserDetailsImpl) userDetails;
        List<PurchasedPackage> purchasedPackages = purchasedPackageService.getAllPurchasedPackagesForUser(userDetail.getUser().getId());
        return purchasedPackageMapper.toDTOList(purchasedPackages);
    }

}
