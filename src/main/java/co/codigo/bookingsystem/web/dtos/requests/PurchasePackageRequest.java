package co.codigo.bookingsystem.web.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePackageRequest {
    @NotNull(message = "Package id must not be null")
    Long packageId;
}
