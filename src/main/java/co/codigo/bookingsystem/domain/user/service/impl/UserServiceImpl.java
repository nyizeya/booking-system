package co.codigo.bookingsystem.domain.user.service.impl;

import co.codigo.bookingsystem.common.exceptions.InvalidOperationException;
import co.codigo.bookingsystem.common.utils.CommonUtils;
import co.codigo.bookingsystem.domain.user.entity.EmailVerificationToken;
import co.codigo.bookingsystem.domain.user.entity.PasswordResetToken;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.repository.UserRepository;
import co.codigo.bookingsystem.domain.user.service.EmailVerificationTokenService;
import co.codigo.bookingsystem.domain.user.service.PasswordResetTokenService;
import co.codigo.bookingsystem.domain.user.service.UserService;
import co.codigo.bookingsystem.web.dtos.response.UserDTO;
import co.codigo.bookingsystem.web.dtos.mappers.UserMapper;
import co.codigo.bookingsystem.web.dtos.requests.ResetPasswordRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static co.codigo.bookingsystem.common.constants.UrlConstant.AUTH_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Value("${host.url}")
    private String host;

    private final UserMapper userMapper;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailVerificationTokenService emailVerificationTokenService;

    @Transactional
    @Override
    public User createNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);
        return user;
    }

    @Transactional
    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public User registerUser(User user) {
        if (user.getPassword() != null)
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userMapper.toDTOList(userRepository.findAll());
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> CommonUtils.createEntityNotFoundException("User", "id", id));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> CommonUtils.createEntityNotFoundException("User", "username", username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<UserDTO> findAllUsers() {
        return userMapper.toDTOList(userRepository.findAll());
    }

    @Override
    public User getUserFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return getUserByUsername(userDetails.getUsername());
    }

    @Transactional
    @Override
    public String generateEmailVerificationToken(String email) {
        log.info("Creating a email verification token...");
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> CommonUtils.createEntityNotFoundException("User", "email", email));
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);
        EmailVerificationToken emailVerificationToken = new EmailVerificationToken(user, token, expiryDate);
        emailVerificationTokenService.saveEmailVerificationToken(emailVerificationToken);

        String resetUrl = host + AUTH_URL + "/reset-password?token=" + token;

        try {
            emailService.sendEmail(user.getEmail(), resetUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }

        return token;
    }

    @Transactional
    @Override
    public String generatePasswordResetToken(String email) {
        log.info("Creating a password reset token...");
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> CommonUtils.createEntityNotFoundException("User", "email", email));
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);
        PasswordResetToken passwordResetToken = new PasswordResetToken(user, token, expiryDate);
        passwordResetTokenService.savePasswordResetToken(passwordResetToken);

        String resetUrl = host + AUTH_URL + "/reset-password?token=" + token;

        try {
            emailService.sendEmail(user.getEmail(), resetUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }

        return token;
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken passwordResetToken = passwordResetTokenService.findByToken(request.getToken()).orElseThrow(() ->
                CommonUtils.createEntityNotFoundException("Password request token", "token", request.getToken()));

        if (passwordResetToken.isUsed())
            throw new InvalidOperationException("Password reset token has already been used.");

        if (passwordResetToken.getExpiryDate().isBefore(Instant.now()))
            throw new InvalidOperationException("Password reset token has expired.");

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        passwordResetToken.setUsed(true);
        passwordResetTokenService.savePasswordResetToken(passwordResetToken);
    }

    @Transactional
    @Override
    public boolean verifyEmail(String token) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByToken(token).orElseThrow(() ->
                CommonUtils.createEntityNotFoundException("Email verification request token", "token", token));

        if (emailVerificationToken.isUsed())
            throw new InvalidOperationException("Email verification token has already been used.");

        if (emailVerificationToken.getExpiryDate().isBefore(Instant.now()))
            throw new InvalidOperationException("Email verification token has expired.");

        User user = emailVerificationToken.getUser();
        user.setEmailVerified(true);
        emailVerificationToken.setUsed(true);
        emailVerificationTokenService.saveEmailVerificationToken(emailVerificationToken);
        userRepository.save(user);
        return true;
    }
}