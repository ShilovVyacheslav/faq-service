package io.knowledgebase.demo.service.jwt;

import io.knowledgebase.demo.config.props.JwtProps;
import io.knowledgebase.demo.dto.auth.JwtUserInfoDto;
import io.knowledgebase.demo.dto.auth.LoginRequestDto;
import io.knowledgebase.demo.dto.auth.LoginResponseDto;
import io.knowledgebase.demo.entity.User;
import io.knowledgebase.demo.enums.Role;
import io.knowledgebase.demo.exception.AuthException;
import io.knowledgebase.demo.exception.UserException;
import io.knowledgebase.demo.security.SecurityUser;
import io.knowledgebase.demo.service.jwt.impl.AuthServiceImpl;
import io.knowledgebase.demo.service.jwt.impl.SecurityUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Base64;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityUserService securityUserService;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProps jwtProps;

    @InjectMocks
    private AuthServiceImpl authService;

    private final String TEST_USERNAME = "test_user";
    private final String TEST_PASSWORD = "Password123@";
    private final String TEST_TOKEN = "header.payload.signature";
    private final Long TEST_EXPIRATION = 3600000L;
    private final Long TEST_USER_ID = 1L;
    private final Set<String> TEST_ROLES = Collections.singleton(Role.USER.toString());

    private SecurityUser securityUser;
    private Authentication authentication;
    private Jwt jwt;
    private String basicAuth;

    @BeforeEach
    void init() {
        basicAuth = "Basic " + Base64.getEncoder().encodeToString((TEST_USERNAME + ":" + TEST_PASSWORD).getBytes());
        authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(securityUser);
        jwt = mock(Jwt.class);
    }

    @Test
    @Order(1)
    @DisplayName("[1] Login with valid credentials -> returns LoginResponseDto")
    void login_WithValidCredentials_ReturnsLoginResponseDto() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateAccessToken(securityUser)).thenReturn(TEST_TOKEN);
        when(jwtProps.getAccessExpiration()).thenReturn(TEST_EXPIRATION);

        LoginResponseDto loginResponseDto = authService.login(basicAuth);

        assertThat(loginResponseDto).isNotNull();
        assertEquals(TEST_TOKEN, loginResponseDto.getToken());
        assertEquals("Bearer", loginResponseDto.getTokenType());
        assertEquals(TEST_EXPIRATION, loginResponseDto.getExpiresIn());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(TEST_USERNAME, TEST_PASSWORD)
        );
        verify(jwtService).generateAccessToken(securityUser);
        verifyNoMoreInteractions(authenticationManager, jwtService, securityUserService);

    }

    @Test
    @Order(2)
    @DisplayName("[2] Login with invalid credentials -> throws AuthException")
    void login_WithInvalidCredentials_ThrowsAuthException() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid Credentials"));

        assertThrows(AuthException.class, () -> authService.login(basicAuth));

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(TEST_USERNAME, TEST_PASSWORD)
        );
        verifyNoInteractions(jwtService);

    }

    @Test
    @Order(3)
    @DisplayName("[3] Login with non existent user -> throws UserException")
    void login_WithNonExistentUser_ThrowsUserException() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(UserException.userNotFound(TEST_USERNAME));

        assertThatThrownBy(() -> authService.login(basicAuth))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService);

    }

    @Test
    @Order(4)
    @DisplayName("[4] Login with inactive user -> throw AuthException")
    void login_WithInactiveUser_ThrowsAuthException() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(AuthException.accessDenied());

        assertThatThrownBy(() -> authService.login(basicAuth))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Access denied");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService);

    }

    @Test
    @Order(5)
    @DisplayName("[5] Refresh token with valid jwt -> returns LoginResponseDto")
    void refreshToken_WithValidJwt_ReturnsLoginResponseDto() {

        when(jwt.getSubject()).thenReturn(TEST_USERNAME);
        when(securityUserService.loadUserByUsername(TEST_USERNAME)).thenReturn(securityUser);
        when(jwtService.generateAccessToken(securityUser)).thenReturn(TEST_TOKEN);
        when(jwtProps.getAccessExpiration()).thenReturn(TEST_EXPIRATION);

        LoginResponseDto loginResponseDto = authService.refreshToken(jwt);

        assertThat(loginResponseDto).isNotNull();
        assertEquals(TEST_TOKEN, loginResponseDto.getToken());
        assertEquals("Bearer", loginResponseDto.getTokenType());
        assertEquals(TEST_EXPIRATION, loginResponseDto.getExpiresIn());

        verify(securityUserService).loadUserByUsername(TEST_USERNAME);
        verify(jwtService).generateAccessToken(securityUser);
        verifyNoMoreInteractions(securityUserService, jwtService, authenticationManager);

    }

    @Test
    @Order(6)
    @DisplayName("[6] Refresh token with non-existent user -> throws UserException")
    void refreshToken_WithNonExistentUser_ThrowsUserException() {

        when(jwt.getSubject()).thenReturn(TEST_USERNAME);
        when(securityUserService.loadUserByUsername(TEST_USERNAME))
                .thenThrow(UserException.userNotFound(TEST_USERNAME));

        assertThatThrownBy(() -> authService.refreshToken(jwt))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("not found");

        verify(securityUserService).loadUserByUsername(TEST_USERNAME);
        verifyNoInteractions(jwtService);

    }

    @Test
    @Order(7)
    @DisplayName("[7] Refresh token with inactive user -> throws AuthException")
    void refreshToken_WithInactiveUser_ThrowsAuthException() {

        when(jwt.getSubject()).thenReturn(TEST_USERNAME);
        when(securityUserService.loadUserByUsername(TEST_USERNAME)).thenThrow(AuthException.accessDenied());

        assertThatThrownBy(() -> authService.refreshToken(jwt))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Access denied");

        verify(securityUserService).loadUserByUsername(TEST_USERNAME);
        verifyNoInteractions(jwtService);

    }

    @Test
    @Order(8)
    @DisplayName("[8] Get info by jwt with authenticate user -> returns JwtUserInfoDto")
    void getInfo_WithValidJwt_ReturnsJwtUserInfoDto() {

        when(jwtService.getCurrentUserId()).thenReturn(TEST_USER_ID);
        when(jwtService.getCurrentUserRoles()).thenReturn(TEST_ROLES);

        JwtUserInfoDto jwtUserInfoDto = authService.getInfoByJwt();

        assertThat(jwtUserInfoDto).isNotNull();
        assertEquals(TEST_USER_ID, jwtUserInfoDto.getId());
        assertEquals(TEST_ROLES, jwtUserInfoDto.getRoles());

        verify(jwtService).getCurrentUserId();
        verify(jwtService).getCurrentUserRoles();
        verifyNoMoreInteractions(authenticationManager, jwtService, securityUserService);

    }

    @Test
    @Order(9)
    @DisplayName("[9] Get info by jwt with unauthenticated user -> returns null values")
    void getInfo_WithUnauthenticatedUser_ReturnsNullValues() {

        when(jwtService.getCurrentUserId()).thenReturn(null);
        when(jwtService.getCurrentUserRoles()).thenReturn(Collections.emptySet());

        JwtUserInfoDto jwtUserInfoDto = authService.getInfoByJwt();

        assertThat(jwtUserInfoDto).isNotNull();
        assertThat(jwtUserInfoDto.getId()).isNull();
        assertTrue(jwtUserInfoDto.getRoles().isEmpty());

        verify(jwtService).getCurrentUserId();
        verify(jwtService).getCurrentUserRoles();

    }

    @Test
    @Order(10)
    @DisplayName("[10] Login with LoginRequestDto as null -> throws Exception")
    void login_WithNullLoginRequestDto_ThrowsException() {
        assertThatThrownBy(() -> authService.login("InvalidFormat"))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @Order(11)
    @DisplayName("[11] Refresh with null jwt -> throws Exception")
    void refresh_WithNullJwt_ThrowsException() {
        assertThatThrownBy(() -> authService.refreshToken(null)).isInstanceOf(NullPointerException.class);
    }

}
