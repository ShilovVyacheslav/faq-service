package io.knowledgebase.demo.controller.auth;

import io.knowledgebase.demo.dto.auth.JwtUserInfoDto;
import io.knowledgebase.demo.dto.auth.LoginResponseDto;
import io.knowledgebase.demo.service.jwt.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(authService.login(authorization));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authService.refreshToken(jwt));
    }

    @GetMapping("/info")
    public ResponseEntity<JwtUserInfoDto> getInfo() {
        return ResponseEntity.ok(authService.getInfoByJwt());
    }

}
