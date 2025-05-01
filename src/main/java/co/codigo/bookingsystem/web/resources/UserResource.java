package co.codigo.bookingsystem.web.resources;

import co.codigo.bookingsystem.security.service.UserDetailsImpl;
import co.codigo.bookingsystem.web.dtos.response.UserDTO;
import co.codigo.bookingsystem.web.dtos.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static co.codigo.bookingsystem.common.constants.UrlConstant.USER_URL;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(USER_URL)
public class UserResource {

    private final UserMapper userMapper;

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserDetails(@AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsImpl userDetail = (UserDetailsImpl) userDetails;
        return ResponseEntity.ok(userMapper.toDTO(userDetail.getUser()));
    }
}