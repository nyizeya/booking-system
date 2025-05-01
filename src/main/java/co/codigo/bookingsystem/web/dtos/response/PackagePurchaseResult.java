package co.codigo.bookingsystem.web.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackagePurchaseResult {
    private boolean success;
    private String message;
    private int creditsAdded;
}