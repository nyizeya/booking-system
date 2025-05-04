package co.codigo.bookingsystem.web.resources;

import co.codigo.bookingsystem.domain.booking.entity.Booking;
import co.codigo.bookingsystem.domain.booking.service.BookingService;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.classschedule.service.ClassScheduleService;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import co.codigo.bookingsystem.domain.waitlist.service.WaitlistService;
import co.codigo.bookingsystem.security.service.UserDetailsImpl;
import co.codigo.bookingsystem.web.dtos.mappers.BookingMapper;
import co.codigo.bookingsystem.web.dtos.mappers.ClassScheduleMapper;
import co.codigo.bookingsystem.web.dtos.mappers.WaitlistMapper;
import co.codigo.bookingsystem.web.dtos.requests.BookingRequest;
import co.codigo.bookingsystem.web.dtos.requests.CreateClassScheduleRequest;
import co.codigo.bookingsystem.web.dtos.requests.CreateWaitlistRequest;
import co.codigo.bookingsystem.web.dtos.response.BookingDto;
import co.codigo.bookingsystem.web.dtos.response.ClassScheduleDto;
import co.codigo.bookingsystem.web.dtos.response.MessageResponseDTO;
import co.codigo.bookingsystem.web.dtos.response.WaitlistDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private final BookingMapper bookingMapper;
    private final BookingService bookingService;
    private final WaitlistMapper waitlistMapper;
    private final WaitlistService waitlistService;
    private final ClassScheduleService classScheduleService;
    private final ClassScheduleMapper classScheduleMapper;

    @Operation(summary = "Get all upcoming classes", description = "Get all classes that are not started yet.")
    @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval of upcoming classes",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ClassScheduleDto.class))
            )
    )
    @GetMapping
    public ResponseEntity<List<ClassScheduleDto>> getAllClassSchedules() {
        List<ClassSchedule> classSchedules = classScheduleService.getAllActiveClasses();
        return ResponseEntity.ok(classScheduleMapper.toDTOList(classSchedules));
    }

    @Operation(
            summary = "Create a class schedule",
            description = "Creates a new class schedule. Only accessible to users with ROLE_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Class schedule created successfully",
                    content = @Content(schema = @Schema(implementation = ClassScheduleDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or validation error",
                    content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ClassScheduleDto> createClassSchedule(
            @Valid @RequestBody CreateClassScheduleRequest request
    ) {
        ClassSchedule classSchedule = classScheduleService.createClass(request);
        return ResponseEntity.ok(classScheduleMapper.toDTO(classSchedule));
    }


    @Operation(
            summary = "Cancel a booking",
            description = "Cancels an existing booking. Only the user who made the booking can cancel it."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking cancelled successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid booking ID or bad request",
                    content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized request, authentication required"
            )
    })
    @GetMapping("/cancel-booking")
    public ResponseEntity<MessageResponseDTO> cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("bookingId") Long bookingId
    ) {
        Long userId = ((UserDetailsImpl) userDetails).getUser().getId();
        bookingService.cancelBooking(userId, bookingId);
        return ResponseEntity.ok(new MessageResponseDTO("Booking cancelled successfully"));
    }


    @Operation(
            summary = "Book a class",
            description = "Allows a user to book a class. The request must include the class details, and the user must be authenticated."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking successful",
                    content = @Content(schema = @Schema(implementation = BookingDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation error",
                    content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized, authentication required"
            )
    })
    @PostMapping("/book")
    public ResponseEntity<BookingDto> bookClass(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BookingRequest bookingRequest
    ) {
        User user = ((UserDetailsImpl) userDetails).getUser();
        Booking booking = bookingService.createBooking(user.getId(), bookingRequest);
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }


    @Operation(
            summary = "Add user to the waitlist",
            description = "Allows an authenticated user to be added to the waitlist for a specific class."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User successfully added to the waitlist",
                    content = @Content(schema = @Schema(implementation = WaitlistDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation error",
                    content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized, authentication required"
            )
    })
    @PostMapping("/add-to-waitlist")
    public ResponseEntity<WaitlistDto> addToWaitlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateWaitlistRequest request
    ) {
        Long userId = ((UserDetailsImpl) userDetails).getUser().getId();
        Waitlist waitlist = waitlistService.addToWaitlist(userId, request);
        return ResponseEntity.ok(waitlistMapper.toDTO(waitlist));
    }

}
