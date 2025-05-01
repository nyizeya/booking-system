package co.codigo.bookingsystem.domain.user.service;

import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.web.dtos.response.UserDTO;
import co.codigo.bookingsystem.web.dtos.requests.ResetPasswordRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createNewUser(User user);

    User registerUser(User user);

    List<UserDTO> getAllUsers();

    User getUserById(Long id);

    User getUserByUsername(String username);

    Optional<User> getUserByEmail(String email);

    List<UserDTO> findAllUsers();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User getUserFromAuthentication();

    String generatePasswordResetToken(String email);

    String generateEmailVerificationToken(String email);

    void resetPassword(ResetPasswordRequest request);

    boolean verifyEmail(String token);
}