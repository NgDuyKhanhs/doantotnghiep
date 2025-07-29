package lms.doantotnghiep.controller;

import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lms.doantotnghiep.dto.ResponseLoginDTO;
import lms.doantotnghiep.dto.TokenResponse;
import lms.doantotnghiep.dto.UserDTO;
import lms.doantotnghiep.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
//@Validated
@Slf4j
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody UserDTO userDTO) {
        ResponseLoginDTO response = userService.login(userDTO);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, response.getRefreshCookie().toString())
                .body(Map.of("accessToken", response.getAccessToken()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody UserDTO userDTO) {
        userService.register(userDTO);
        return new ResponseEntity<>("Người dùng đã được tạo thành công, kiểm tra email của bạn để kích hoạt", HttpStatus.OK);
    }

    @PostMapping("/active")
    public ResponseEntity<?> activeAccount(
            @RequestBody String token) {
        boolean isActive = userService.activeAccount(token);
        return new ResponseEntity<>(
                isActive,
                HttpStatus.OK
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) throws ParseException, JOSEException {
        TokenResponse response = userService.refreshToken(refreshToken);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // xoá cookie
                .build();
        request.getSession().invalidate();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("message", "Logout success"));
    }
}
