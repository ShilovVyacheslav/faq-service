package io.knowledgebase.demo.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.knowledgebase.demo.config.props.JwtProps;
import io.knowledgebase.demo.entity.User;
import io.knowledgebase.demo.enums.Role;
import io.knowledgebase.demo.security.SecurityUser;
import io.knowledgebase.demo.service.jwt.impl.JwtServiceImpl;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JwtServiceTest {

    @Mock
    private JwtProps jwtProps;

    @InjectMocks
    private JwtServiceImpl jwtService;

    private static final String TEST_USERNAME = "test_user";
    private static final Long TEST_USER_ID = 1L;
    private static final Role TEST_ROLE = Role.ADMIN;
    private static final String SECRET_KEY = "uG4PzXwR7VjQ3LkH0bTnE8cYfM2aS9dZpW5rU1xO6hNqC7vJtB4yF3mK8gD2lP0oA"; // pragma: allowlist secret
    private static final long EXPIRATION_TIME = 3600000L;
    private static final String PATTERN = "^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$";

    @BeforeEach
    void init() {
        lenient().when(jwtProps.getSecretKey()).thenReturn(SECRET_KEY);
        lenient().when(jwtProps.getAccessExpiration()).thenReturn(EXPIRATION_TIME);
        jwtService = new JwtServiceImpl(jwtProps);
    }

    @Test
    @Order(1)
    @DisplayName("[1] Generate valid access token")
    void generateAccessToken_ValidUser_ReturnsToken() {

        SecurityUser securityUser = createValidSecurityUser();
        String token = jwtService.generateAccessToken(securityUser);

        assertThat(token).isNotBlank().containsPattern(PATTERN);

        Claims claims = parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(TEST_USERNAME);
        assertThat(((Number) claims.get("id")).longValue()).isEqualTo(securityUser.getUser().getId());
        assertThat(claims.get("roles"))
                .isInstanceOf(List.class)
                .asInstanceOf(InstanceOfAssertFactories.list(String.class))
                .allSatisfy(role -> assertThat(role).isEqualToIgnoringCase(
                                securityUser.getUser().getRole().toString()
                        )
                );

    }

    @Test
    @Order(2)
    @DisplayName("[2] Valid token returns true")
    void isTokenValid_ValidToken_ReturnsTrue() {

        SecurityUser securityUser = createValidSecurityUser();
        String token = jwtService.generateAccessToken(securityUser);

        assertThat(jwtService.isTokenValid(token, securityUser)).isTrue();

    }

    @Test
    @Order(3)
    @DisplayName("[3] Expired token returns false")
    void isTokenValid_ExpiredToken_ReturnsFalse() throws InterruptedException {

        when(jwtProps.getAccessExpiration()).thenReturn(1L);

        jwtService = new JwtServiceImpl(jwtProps);

        String token = jwtService.generateAccessToken(createValidSecurityUser());

        Thread.sleep(1000L);

        assertThat(jwtService.isTokenValid(token, createValidSecurityUser())).isFalse();

    }

    @Test
    @Order(4)
    @DisplayName("[4] Token with different roles returns false")
    void isTokenValid_DifferentRoles_ReturnsFalse() {

        SecurityUser securityUser1 = createValidSecurityUser();
        securityUser1.getUser().setRole(Role.EXPERT);
        SecurityUser securityUser2 = createValidSecurityUser();
        securityUser2.getUser().setRole(Role.USER);

        String token = jwtService.generateAccessToken(securityUser1);

        assertThat(jwtService.isTokenValid(token, securityUser2)).isFalse();

    }

    @Test
    @Order(5)
    @DisplayName("[5] Invalid token returns false")
    void isTokenValid_InvalidToken_ReturnsFalse() {
        assertThat(jwtService.isTokenValid("invalid_token", createValidSecurityUser())).isFalse();
    }

    @Test
    @Order(6)
    @DisplayName("[6] Extract username from valid token")
    void extractUsername_ValidToken_ReturnsUsername() {

        String token = jwtService.generateAccessToken(createValidSecurityUser());

        assertThat(jwtService.extractUsername(token)).isEqualTo(TEST_USERNAME);

    }

    @Test
    @Order(7)
    @DisplayName("[7] Extract username from invalid token throws JwtException")
    void extractUsername_InvalidToken_ThrowsJwtException() {
        assertThatThrownBy(() -> jwtService.extractUsername("invalid_token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @Order(8)
    @DisplayName("[8] Get current user ID from context")
    void getCurrentUserId_Authenticated_ReturnsId() {
        try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
            Jwt jwt = mock(Jwt.class);
            when(jwt.getClaims()).thenReturn(Map.of("id", TEST_USER_ID));

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(jwt);

            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);

            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            assertThat(jwtService.getCurrentUserId()).isEqualTo(TEST_USER_ID);
        }
    }

    @Test
    @Order(9)
    @DisplayName("[9] Get current user roles from context")
    void getCurrentUserRoles_Authenticated_ReturnsRoles() {
        try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
            Jwt jwt = mock(Jwt.class);
            when(jwt.getClaims()).thenReturn(Map.of("roles", List.of(TEST_ROLE.name())));

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(jwt);

            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);

            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            assertThat(jwtService.getCurrentUserRoles())
                    .asInstanceOf(InstanceOfAssertFactories.collection(String.class))
                    .containsExactlyInAnyOrder(TEST_ROLE.toString());
        }
    }

    private SecurityUser createValidSecurityUser() {
        return new SecurityUser(
                new User(
                        TEST_USER_ID,
                        null,
                        TEST_USERNAME,
                        null,
                        null,
                        TEST_ROLE,
                        null,
                        null,
                        null
                )
        );
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
