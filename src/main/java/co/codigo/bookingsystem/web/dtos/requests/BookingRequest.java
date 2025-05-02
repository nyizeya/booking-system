package co.codigo.bookingsystem.web.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    @NotNull(message = "Class id is required for booking")
    private Long classId;
    @NotNull(message = "Package id is required for booking")
    private Long packageId;
}