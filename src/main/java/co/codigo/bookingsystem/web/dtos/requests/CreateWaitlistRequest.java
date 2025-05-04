package co.codigo.bookingsystem.web.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWaitlistRequest {
    @NotNull(message = "Class id must not be null")
    private Long classId;

    @NotNull(message = "Package id must not be null")
    private Long packageId;
}
