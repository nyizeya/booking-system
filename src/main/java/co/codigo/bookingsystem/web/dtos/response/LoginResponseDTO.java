package co.codigo.bookingsystem.web.dtos.response;

import lombok.Data;

import java.util.Set;

@Data
public class LoginResponseDTO {
    private String username;
    private String jwtToken;
    private String error;
    private Set<String> roles;

    public LoginResponseDTO(String error) {
        this.error = error;
    }

    public LoginResponseDTO(String username, String jwtToken, Set<String> roles) {
        this.username = username;
        this.jwtToken = jwtToken;
        this.roles = roles;
    }
}