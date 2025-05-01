package co.codigo.bookingsystem.web.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {

    @Size(min = 3, max = 20)
    @NotBlank(message = "Username must be at least 3 characters long")
    private String username;

    @Email(message = "Enter a valid email")
    @NotBlank(message = "Email must not be blank")
    private String email;

    @Size(min = 4, max = 40)
    @NotBlank(message = "Password must be at least 4 characters long")
    private String password;

    @Size(min = 2, max = 2)
    @NotBlank(message = "Country code must be provided")
    private String countryCode;

}