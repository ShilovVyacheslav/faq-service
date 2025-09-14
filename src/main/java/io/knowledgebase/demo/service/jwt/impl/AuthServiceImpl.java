package io.knowledgebase.demo.service.jwt.impl;

import io.knowledgebase.demo.common.util.AuthUtil;
import io.knowledgebase.demo.config.props.JwtProps;
import io.knowledgebase.demo.dto.auth.JwtUserInfoDto;
import io.knowledgebase.demo.dto.auth.LoginRequestDto;
import io.knowledgebase.demo.dto.auth.LoginResponseDto;
import io.knowledgebase.demo.exception.AuthException;
import io.knowledgebase.demo.security.SecurityUser;
import io.knowledgebase.demo.service.jwt.AuthService;
import io.knowledgebase.demo.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final SecurityUserService securityUserService;
    private final JwtService jwtService;
    private final JwtProps jwtProps;

    @Override
    public LoginResponseDto login(String authorization) {

        LoginRequestDto loginRequestDto = AuthUtil.base64ToLoginRequestDto(authorization);

        log.info("Attempting to login user: {}", loginRequestDto.getUsername());

        SecurityUser securityUser = (SecurityUser) authenticate(loginRequestDto).getPrincipal();

        log.debug("User {} authenticated successfully", loginRequestDto.getUsername());

        LoginResponseDto response = buildLoginResponse(jwtService.generateAccessToken(securityUser));

        log.info("Login successful for user: {}", loginRequestDto.getUsername());

        return response;
    }

    @Override
    public LoginResponseDto refreshToken(Jwt jwt) {

        String username = jwt.getSubject();

        log.debug("Attempting to refresh token for user: {}", username);

        SecurityUser securityUser = (SecurityUser) securityUserService.loadUserByUsername(username);

        LoginResponseDto response = buildLoginResponse(jwtService.generateAccessToken(securityUser));

        log.debug("Token refreshed successfully for user: {}", username);

        return response;
    }

    @Override
    public JwtUserInfoDto getInfoByJwt() {
        return JwtUserInfoDto.builder()
                .id(jwtService.getCurrentUserId())
                .roles(jwtService.getCurrentUserRoles())
                .build();
    }

    private Authentication authenticate(LoginRequestDto dto) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw AuthException.invalidCredentials();
        }
    }

    private LoginResponseDto buildLoginResponse(String accessToken) {
        return LoginResponseDto.builder()
                .token(accessToken)
                .expiresIn(jwtProps.getAccessExpiration())
                .build();
    }

}

