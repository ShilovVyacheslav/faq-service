package io.knowledgebase.demo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.knowledgebase.demo.dto.ErrorDto;
import io.knowledgebase.demo.exception.AuthException;
import io.knowledgebase.demo.security.SecurityUser;
import io.knowledgebase.demo.service.jwt.JwtService;
import io.knowledgebase.demo.service.jwt.impl.SecurityUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SecurityUserService securityUserService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws IOException {

        try {
            Optional.ofNullable(extractJwtFromHeader(request))
                    .ifPresent(jwt -> authenticateUser(jwt, request));
            filterChain.doFilter(request, response);
        } catch (AuthException ex) {
            handleAuthException(response, ex);
        } catch (UsernameNotFoundException ex) {
            handleAuthException(response, AuthException.invalidCredentials());
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT validation error: {}", ex.getMessage());
            handleAuthException(response, AuthException.jwtAuth());
        } catch (Exception ex) {
            log.error("Unexpected authentication error", ex);
            handleAuthException(response, AuthException.jwtSystem());
        }

    }

    private String extractJwtFromHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7))
                .orElse(null);
    }

    private void authenticateUser(String jwt, HttpServletRequest request) {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.trace("User already authenticated, skipping JWT validation");
            return;
        }

        String username = jwtService.extractUsername(jwt);
        if (username == null) {
            throw AuthException.jwtAuth();
        }

        SecurityUser securityUser = (SecurityUser) securityUserService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(jwt, securityUser)) {
            log.warn("Invalid JWT token for user: {}", username);
            throw AuthException.jwtAuth();
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                securityUser, null, securityUser.getAuthorities()
        );

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void handleAuthException(HttpServletResponse response, AuthException ex) throws IOException {

        ErrorDto error = new ErrorDto(
                ex.getCode(),
                ex.getMessage(),
                ex.getErrorType(),
                ex.getServiceName(),
                ex.getDetails()
        );

        response.setContentType("application/json");
        response.setStatus(ex.getStatus().value());
        objectMapper.writeValue(response.getWriter(), error);
    }

}

