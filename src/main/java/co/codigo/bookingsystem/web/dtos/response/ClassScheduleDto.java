package co.codigo.bookingsystem.web.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassScheduleDto {
    private Long id;
    private String name;
    private String description;
    private String countryCode;
    private int requiredCredits;
    private int availableSlots;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}