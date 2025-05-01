package co.codigo.bookingsystem.web.dtos.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackagePurchaseRequest {
    @NotNull
    private Long packageId;
    
    @NotBlank
    private String paymentMethod;
    
    private String paymentReference;
}