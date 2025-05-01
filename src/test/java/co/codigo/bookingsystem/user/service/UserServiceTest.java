package co.codigo.bookingsystem.user.service;

import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@Slf4j
@Rollback
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void createNewUser() {
        User nyizeya = new User("nyizeya", "nyizeya@gmail.com", "test123");
        log.info("Before created: {}", nyizeya);
        nyizeya = userService.createNewUser(nyizeya);
        log.info("After created: {}", nyizeya);
    }
}