package co.codigo.bookingsystem.web.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackagePlanDto {
    private Long id;
    private String name;
    private String countryCode;
    private int credits;
    private BigDecimal price;
    private int expiryDays;
}