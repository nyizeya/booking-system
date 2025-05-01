package co.codigo.bookingsystem.domain.user.service;

import co.codigo.bookingsystem.domain.user.entity.EmailVerificationToken;

import java.util.Optional;

public interface EmailVerificationTokenService {
    void saveEmailVerificationToken(EmailVerificationToken passwordResetToken);

    Optional<EmailVerificationToken> findByToken(String token);
}