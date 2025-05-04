package co.codigo.bookingsystem.web.resources;

import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.packageplan.entity.UserPackage;
import co.codigo.bookingsystem.domain.packageplan.service.PackagePlanService;
import co.codigo.bookingsystem.domain.packageplan.service.UserPackageService;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.security.service.UserDetailsImpl;
import co.codigo.bookingsystem.web.dtos.mappers.PackagePlanMapper;
import co.codigo.bookingsystem.web.dtos.mappers.UserPackageMapper;
import co.codigo.bookingsystem.web.dtos.requests.CreatePackagePlanRequest;
import co.codigo.bookingsystem.web.dtos.requests.PurchasePackageRequest;
import co.codigo.bookingsystem.web.dtos.response.ClassScheduleDto;
import co.codigo.bookingsystem.web.dtos.response.MessageResponseDTO;
import co.codigo.bookingsystem.web.dtos.response.PackagePlanDto;
import co.codigo.bookingsystem.web.dtos.response.UserPackageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static co.codigo.bookingsystem.common.constants.UrlConstant.PACKAGE_PLAN_URL;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PACKAGE_PLAN_URL)
public class PackagePlanResource {

    private final UserPackageMapper userPackageMapper;
    private final PackagePlanMapper packagePlanMapper;
    private final PackagePlanService packagePlanService;
    private final UserPackageService userPackageService;

    @Operation(
            summary = "Create a new package plan",
            description = "Allows an admin to create a new package plan."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Package plan created successfully",
                    content = @Content(schema = @Schema(implementation = PackagePlanDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation error",
                    content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized, admin role required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden, user does not have admin privileges"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<PackagePlanDto> createPackagePlan(
            @Valid @RequestBody CreatePackagePlanRequest request
    ) {
        PackagePlan packagePlan = packagePlanService.createPackage(request);
        return ResponseEntity.ok(packagePlanMapper.toDTO(packagePlan));
    }


    @Operation(
            summary = "Purchase a package",
            description = "Allows a user to purchase a package by specifying the package ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Package purchased successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation error",
                    content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized, user must be logged in"
            )
    })
    @PostMapping("/purchase")
    public ResponseEntity<MessageResponseDTO> purchasePackage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PurchasePackageRequest request
    ) {
        Long userId = ((UserDetailsImpl) userDetails).getUser().getId();
        userPackageService.purchasePackage(userId, request.getPackageId());
        return ResponseEntity.ok().body(new MessageResponseDTO("Package purchased successfully"));
    }

    @Operation(
            summary = "Get available packages for a user",
            description = "Retrieve a list of all available package plans that the authenticated user hasn't bought."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved available packages",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PackagePlanDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized, user must be logged in"
            )
    })
    @GetMapping("/available")
    public ResponseEntity<List<PackagePlanDto>> getActivePackagesForUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = ((UserDetailsImpl) userDetails).getUser();
        List<PackagePlan> packagePlans = packagePlanService.findAllAvailablePackagesForUser(user.getId());
        return ResponseEntity.ok(packagePlanMapper.toDTOList(packagePlans));
    }

    @Operation(
            summary = "Get all active package plans",
            description = "Retrieve a list of all active package plans available in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all active package plans",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PackagePlanDto.class))
                    )
            )
    })
    @GetMapping("/all")
    public ResponseEntity<List<PackagePlanDto>> getAllActivePackages() {
        List<PackagePlan> packagePlans = packagePlanService.getAllActivePackages();
        return ResponseEntity.ok(packagePlanMapper.toDTOList(packagePlans));
    }


    @Operation(
            summary = "Get all purchased packages for the current user",
            description = "Retrieve a list of all purchased packages for the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all purchased packages for the user",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserPackageDto.class))
                    )
            )
    })
    @GetMapping("/purchased")
    public ResponseEntity<List<UserPackageDto>> getPurchasedPackagesForUser(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((UserDetailsImpl) userDetails).getUser().getId();
        List<UserPackage> purchasedPackages = userPackageService.findAllPurchasedPackages(userId);
        return ResponseEntity.ok(userPackageMapper.toDTOList(purchasedPackages));
    }
}
