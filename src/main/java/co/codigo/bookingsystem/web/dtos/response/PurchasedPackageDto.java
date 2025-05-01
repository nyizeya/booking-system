package co.codigo.bookingsystem.web.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasedPackageDto {
    private Long id;
    private UserDTO userDTO;
    private PackagePlanDto packageDetails;
    private LocalDateTime purchaseDate;
    private LocalDateTime expiryDate;
    private int remainingCredits;
    private boolean expired;
}