package co.codigo.bookingsystem.domain.user.service.impl;

import co.codigo.bookingsystem.domain.user.entity.PasswordResetToken;
import co.codigo.bookingsystem.domain.user.repository.PasswordResetTokenRepository;
import co.codigo.bookingsystem.domain.user.service.PasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenRepository repository;

    @Override
    public void savePasswordResetToken(PasswordResetToken passwordResetToken) {
        repository.save(passwordResetToken);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return repository.findByToken(token);
    }

}