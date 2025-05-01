package co.codigo.bookingsystem.config;

import co.codigo.bookingsystem.domain.audit.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
        "co.codigo.bookingsystem.domain.user.repository",
        "co.codigo.bookingsystem.domain.availableclass.repository",
        "co.codigo.bookingsystem.domain.booking.repository",
        "co.codigo.bookingsystem.domain.packageplan.repository",
        "co.codigo.bookingsystem.domain.purchasedpkg.repository",
        "co.codigo.bookingsystem.domain.waitlist.repository",
})
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class PersistenceConfig {
    @Bean
    AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }
}