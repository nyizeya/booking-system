package co.codigo.bookingsystem.web.dtos.response;

import co.codigo.bookingsystem.common.enumerations.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private AvailableClassDto classDetails;
    private BookingStatus status;
    private LocalDateTime bookedAt;
    private Boolean checkedIn;
}