package co.codigo.bookingsystem.domain.user.service;

import co.codigo.bookingsystem.domain.user.entity.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenService {
    void savePasswordResetToken(PasswordResetToken passwordResetToken);

    Optional<PasswordResetToken> findByToken(String token);
}