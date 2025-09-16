package io.knowledgebase.demo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.knowledgebase.demo.dto.ErrorDto;
import io.knowledgebase.demo.exception.AuthException;
import io.knowledgebase.demo.security.SecurityUser;
import io.knowledgebase.demo.service.jwt.JwtService;
import io.knowledgebase.demo.service.jwt.impl.SecurityUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private SecurityUserService securityUserService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private static final String VALID_TOKEN = "header.payload.signature";
    private static final String INVALID_TOKEN = "invalid.token";
    private static final String TEST_USERNAME = "test_user";
    private static final SecurityUser securityUser = mock(SecurityUser.class);

    @BeforeEach
    void init() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Order(1)
    @DisplayName("[1] Do filter internal with valid token")
    void doFilterInternal_WithValidToken_ShouldAuthenticateUser() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(securityUserService.loadUserByUsername(TEST_USERNAME)).thenReturn(securityUser);
        when(jwtService.isTokenValid(VALID_TOKEN, securityUser)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

        verify(jwtService).extractUsername(VALID_TOKEN);
        verify(securityUserService).loadUserByUsername(TEST_USERNAME);
        verify(jwtService).isTokenValid(VALID_TOKEN, securityUser);
        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(jwtService, filterChain);

    }

    @Test
    @Order(2)
    @DisplayName("[2] Do filter internal with no token")
    void doFilterInternal_WithInvalidToken_ShouldNotAuthenticateUser() throws Exception {

        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, securityUserService);

    }

    @Test
    @Order(3)
    @DisplayName("[3] Do filter internal with invalid token")
    void doFilterInternal_WithInvalidToken_ShouldHandleAuthException() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer " + INVALID_TOKEN);
        when(jwtService.extractUsername(INVALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(securityUserService.loadUserByUsername(TEST_USERNAME)).thenReturn(securityUser);
        when(jwtService.isTokenValid(INVALID_TOKEN, securityUser)).thenReturn(false);
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(response).setContentType("application/json");
        verify(response).setStatus(anyInt());
        verify(objectMapper).writeValue(any(PrintWriter.class), any(ErrorDto.class));
        verifyNoMoreInteractions(filterChain);

    }

    @Test
    @Order(4)
    @DisplayName("[4] Do filter internal with expired token")
    void doFilterInternal_WithExpiredToken_ShouldHandleAuthException() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer " + INVALID_TOKEN);
        when(jwtService.extractUsername(INVALID_TOKEN)).thenThrow(AuthException.jwtAuth());
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setContentType("application/json");
        verify(response).setStatus(anyInt());
        verify(objectMapper).writeValue(any(PrintWriter.class), any(ErrorDto.class));
        verifyNoMoreInteractions(filterChain);

    }

    @Test
    @Order(5)
    @DisplayName("[5] Do filter internal when already authenticated")
    void doFilterInternal_WhenAlreadyAuthenticated_ShouldSkipAuthentication() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(mock(UsernamePasswordAuthenticationToken.class));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, securityUserService);

    }

    @Test
    @Order(6)
    @DisplayName("[6] Do filter internal when user is inactive")
    void doFilterInternal_WhenUserIsInactive_ShouldHandleAuthException() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(securityUserService.loadUserByUsername(TEST_USERNAME)).thenThrow(AuthException.accessDenied());
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setContentType("application/json");
        verify(response).setStatus(anyInt());
        verify(objectMapper).writeValue(any(PrintWriter.class), any(ErrorDto.class));
        verifyNoMoreInteractions(filterChain);

    }

    @Test
    @Order(7)
    @DisplayName("[7] No Authorization header")
    void doFilterInternal_WhenNoAuthorizationHeader_ShouldContinueChain() throws Exception {

        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, securityUserService);

    }

    @Test
    @Order(8)
    @DisplayName("[8] Authorization header without Bearer prefix")
    void doFilterInternal_WithoutBearerPrefix_ShouldContinueChain() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, securityUserService);

    }

    @Test
    @Order(9)
    @DisplayName("[9] Empty jwt should throw AuthException")
    void doFilterInternal_WithEmptyJwt_ShouldThrowAuthException() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setContentType("application/json");
        verify(response).setStatus(anyInt());
        verify(objectMapper).writeValue(any(PrintWriter.class), any(ErrorDto.class));
        verifyNoInteractions(filterChain, securityUserService);

    }

    @Test
    @Order(10)
    @DisplayName("[10] Do filter with valid jwt and username as null")
    void doFilterInternal_WithValidJwtAndUsernameAsNull_ShouldHandleAuthException() throws Exception {

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(null);

        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setContentType("application/json");
        verify(response).setStatus(anyInt());
        verify(objectMapper).writeValue(any(PrintWriter.class), any(ErrorDto.class));
        verifyNoInteractions(filterChain, securityUserService);

    }

}
