package co.codigo.bookingsystem;

import co.codigo.bookingsystem.common.enumerations.AppRole;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.user.service.RoleService;
import co.codigo.bookingsystem.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class BookingSystemApplication {

    private final RoleService roleService;
    private final UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(BookingSystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner init() {
        return args -> {
            if (roleService.findAllRoles().isEmpty()) {
                roleService.createNewRole(AppRole.ROLE_ADMIN);
                roleService.createNewRole(AppRole.ROLE_USER);
            }

            if (userService.findAllUsers().isEmpty()) {
                User adminUser = new User("admin", "admin@gmail.com", "1234");
                adminUser.setRole(roleService.findByRoleName(AppRole.ROLE_ADMIN).get());
                userService.createNewUser(adminUser);

                User user = new User("user", "user@gmail.com", "1234");
                user.setRole(roleService.findByRoleName(AppRole.ROLE_USER).get());
                userService.createNewUser(user);
            }
        };
    }

}
