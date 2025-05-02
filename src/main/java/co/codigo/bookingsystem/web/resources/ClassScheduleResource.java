package co.codigo.bookingsystem.web.resources;

import co.codigo.bookingsystem.domain.booking.entity.Booking;
import co.codigo.bookingsystem.domain.booking.service.BookingService;
import co.codigo.bookingsystem.domain.classschedule.service.ClassScheduleService;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.security.service.UserDetailsImpl;
import co.codigo.bookingsystem.web.dtos.mappers.BookingMapper;
import co.codigo.bookingsystem.web.dtos.mappers.ClassScheduleMapper;
import co.codigo.bookingsystem.web.dtos.requests.BookingRequest;
import co.codigo.bookingsystem.web.dtos.response.BookingDto;
import co.codigo.bookingsystem.web.dtos.response.ClassScheduleDto;
import co.codigo.bookingsystem.web.dtos.response.MessageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static co.codigo.bookingsystem.common.constants.UrlConstant.CLASS_SCHEDULE_URL;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(CLASS_SCHEDULE_URL)
public class ClassScheduleResource {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final ClassScheduleService classScheduleService;
    private final ClassScheduleMapper classScheduleMapper;

    @GetMapping
    public List<ClassScheduleDto> getClassSchedulesByCountryCode(
            @RequestParam("countryCode") String countryCode
    ) {
        return classScheduleMapper.toDTOList(
                classScheduleService.getClassSchedulesByCountryCode(countryCode)
        );
    }

    @GetMapping("/cancel-booking")
    public MessageResponseDTO cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("bookingId") Long bookingId
    ) {
        Long userId = ((UserDetailsImpl) userDetails).getUser().getId();
        bookingService.cancelBooking(userId, bookingId);
        return new MessageResponseDTO("Booking cancelled successfully");
    }

    @PostMapping("/book")
    public BookingDto bookClass(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BookingRequest bookingRequest
    ) {
        User user = ((UserDetailsImpl) userDetails).getUser();
        Booking booking = bookingService.createBooking(user, bookingRequest);
        return bookingMapper.toDTO(booking);
    }

}
