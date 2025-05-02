package co.codigo.bookingsystem.web.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackagePlanRequest {

    @NotBlank(message = "Package name must not be empty")
    private String name;

    @NotBlank(message = "Country code must not be empty")
    @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters long")
    private String countryCode;

    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.00", inclusive = true, message = "Price must be at least 0")
    private BigDecimal price;

    @NotNull(message = "Credit count must not be null")
    @Min(value = 1, message = "Credit count must be greater than 0")
    private Integer creditCount;

    @NotNull(message = "Expiry days must not be null")
    @Min(value = 1, message = "Expiry days must be greater than 0")
    private Integer expiryDays;

    @NotNull(message = "Active status must not be null")
    private Boolean active;
}