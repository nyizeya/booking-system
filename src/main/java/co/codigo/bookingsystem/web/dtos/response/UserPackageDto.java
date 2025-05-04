package co.codigo.bookingsystem.web.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPackageDto {
    private Long userId;
    private Long packageId;
    private String username;
    private String packageName;
    private boolean isExpired;
    private Integer remainingCredits;
}