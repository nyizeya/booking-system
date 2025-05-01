package co.codigo.bookingsystem.domain.user.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {
    public void sendEmail(String email, String content) {
        log.info("sent email to {}, content: {}", email, content);
    }
}
