package co.codigo.bookingsystem.web.resources;

import co.codigo.bookingsystem.common.enumerations.AppRole;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.service.RoleService;
import co.codigo.bookingsystem.domain.user.service.UserService;
import co.codigo.bookingsystem.security.jwt.services.JwtService;
import co.codigo.bookingsystem.web.dtos.response.LoginResponseDTO;
import co.codigo.bookingsystem.web.dtos.response.MessageResponseDTO;
import co.codigo.bookingsystem.web.dtos.response.PasswordResetTokenDTO;
import co.codigo.bookingsystem.web.dtos.response.UserDTO;
import co.codigo.bookingsystem.web.dtos.mappers.UserMapper;
import co.codigo.bookingsystem.web.dtos.requests.LoginRequest;
import co.codigo.bookingsystem.web.dtos.requests.ResetPasswordRequest;
import co.codigo.bookingsystem.web.dtos.requests.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import static co.codigo.bookingsystem.common.constants.UrlConstant.AUTH_URL;

@Slf4j
@RestController
@RequestMapping(AUTH_URL)
@RequiredArgsConstructor
public class AuthorizationResource {

    private final JwtService jwtService;
    private final UserService userService;
    private final RoleService roleService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "Login", description = "Authenticate a user and generate JWT token.")
    @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = LoginResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Bad credentials", content = @Content(schema = @Schema(implementation = MessageResponseDTO.class)))
    @PostMapping("/sign-in")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Bad Credentials"));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        final String jwtToken = jwtService.generateTokenFromUsername(userDetails);
        return ResponseEntity.ok(new LoginResponseDTO(userDetails.getUsername(), jwtToken, userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet())));
    }

    @Operation(summary = "Register", description = "Register a user and send email verification link.")
    @ApiResponse(responseCode = "200", description = "Register successful", content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "400", description = "Bad credentials", content = @Content(schema = @Schema(implementation = MessageResponseDTO.class)))
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        if (userService.existsByUsername(request.getUsername()))
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Error username already exists"));

        if (userService.existsByEmail(request.getEmail()))
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Error email already exists"));

        User user = new User(request.getUsername(), request.getEmail(), request.getPassword());

        user.setRole(roleService.findByRoleName(AppRole.ROLE_USER).get());

        user = userService.createNewUser(user);
        UserDTO dto = userMapper.toDTO(user);
        dto.setEmailVerificationToken(userService.generateEmailVerificationToken(user.getEmail()));
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Email Verification", description = "Verify email from verification token.")
    @ApiResponse(responseCode = "200", description = "Email has been verified successfully", content = @Content(schema = @Schema(implementation = MessageResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Error email verification", content = @Content(schema = @Schema(implementation = MessageResponseDTO.class)))
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @RequestParam String token
    ) {
        try {
            userService.verifyEmail(token);
            return ResponseEntity.ok(new MessageResponseDTO("Email has been verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Error email verification : " + e.getMessage()));
        }
    }

    @Operation(summary = "Forgot Password", description = "Response a password reset token.")
    @ApiResponse(responseCode = "200", description = "Generate password reset token successful", content = @Content(schema = @Schema(implementation = PasswordResetTokenDTO.class)))
    @ApiResponse(responseCode = "400", description = "Error sending password reset email", content = @Content(schema = @Schema(implementation = MessageResponseDTO.class)))
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        log.info("Generate password reset token...");
        final String hostOrError = userService.generatePasswordResetToken(email);

        if ("error".equals(hostOrError)) {
            return ResponseEntity.internalServerError().body(new MessageResponseDTO("Error sending password reset email"));
        }

        PasswordResetTokenDTO body = new PasswordResetTokenDTO(hostOrError);

        return ResponseEntity.ok(body);

    };

    @Operation(summary = "Reset Password", description = "Reset password.")
    @ApiResponse(responseCode = "200", description = "Password has been reset successfully", content = @Content(schema = @Schema(implementation = MessageResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Error reset password : ", content = @Content(schema = @Schema(implementation = MessageResponseDTO.class)))
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        try {
            userService.resetPassword(request);
            return ResponseEntity.ok(new MessageResponseDTO("Password has been reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Error reset password : " + e.getMessage()));
        }
    }

}