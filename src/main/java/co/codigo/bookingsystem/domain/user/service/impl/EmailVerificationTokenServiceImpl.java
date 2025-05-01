package co.codigo.bookingsystem.domain.user.service.impl;

import co.codigo.bookingsystem.domain.user.entity.EmailVerificationToken;
import co.codigo.bookingsystem.domain.user.repository.EmailVerificationTokenRepository;
import co.codigo.bookingsystem.domain.user.service.EmailVerificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationTokenServiceImpl implements EmailVerificationTokenService {

    private final EmailVerificationTokenRepository repository;

    @Override
    public void saveEmailVerificationToken(EmailVerificationToken passwordResetToken) {
        repository.save(passwordResetToken);
    }

    @Override
    public Optional<EmailVerificationToken> findByToken(String token) {
        return repository.findByToken(token);
    }

}