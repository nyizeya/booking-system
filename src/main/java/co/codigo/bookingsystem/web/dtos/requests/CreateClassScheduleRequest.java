package co.codigo.bookingsystem.web.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassScheduleRequest {
    private Long id;

    @NotBlank(message = "Class name must not be blank")
    private String name;

    @NotBlank(message = "Country code must not be blank")
    private String countryCode;

    private String description;

    @NotNull(message = "Required credits must not be null")
    private Integer requiredCredits;

    @NotNull(message = "Max capacity must not be null")
    private Integer maxCapacity;

    @NotNull(message = "Start time must not be null")
    private LocalDateTime startTime;

    @NotNull(message = "End time must not be null")
    private LocalDateTime endTime;
}
