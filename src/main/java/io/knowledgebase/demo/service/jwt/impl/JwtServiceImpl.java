package io.knowledgebase.demo.service.jwt.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.knowledgebase.demo.config.props.JwtProps;
import io.knowledgebase.demo.security.SecurityUser;
import io.knowledgebase.demo.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProps jwtProps;

    @Override
    public String generateAccessToken(SecurityUser securityUser) {
        log.debug("Generating access token for user: {}", securityUser.getUsername());
        return buildAccessToken(securityUser, new HashMap<>());
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isTokenValid(String token, SecurityUser securityUser) {
        try {
            final String username = extractUsername(token);
            return (username.equals(securityUser.getUsername()) &&
                    !isTokenExpired(token) &&
                    areRolesConsistent(token, securityUser));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Long getCurrentUserId() {
        return getJwtPrincipal()
                .map(jwt -> jwt.getClaims().get("id"))
                .map(id -> Long.valueOf(id.toString()))
                .orElse(null);
    }

    @Override
    public Set<String> getCurrentUserRoles() {
        return getJwtPrincipal()
                .map(Jwt::getClaims)
                .map(this::extractRoles)
                .orElse(Collections.emptySet());
    }

    private Optional<Jwt> getJwtPrincipal() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .map(Jwt.class::cast);
    }

    @Override
    public String getUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("sub");
        }
        return null;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    private boolean areRolesConsistent(String token, UserDetails userDetails) {
        Set<String> tokenRoles = extractClaim(token, this::extractRoles);
        Set<String> userRoles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return tokenRoles.equals(userRoles);
    }

    private Set<String> extractRoles(Map<String, Object> claims) {
        try {
            Object rolesClaim = claims.get("roles");
            if (rolesClaim instanceof List<?> list) {
                return list.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());
            }
            return Collections.emptySet();
        } catch (Exception ex) {
            log.debug("Failed to extract roles from token: {}", ex.getMessage());
            return Collections.emptySet();
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token,
                               Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildAccessToken(SecurityUser securityUser, Map<String, Object> claims) {
        claims.put("id", securityUser.getUser().getId());
        claims.put("roles", securityUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        );
        return Jwts
                .builder()
                .claims(claims)
                .subject(securityUser.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProps.getAccessExpiration() * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProps.getSecretKey().getBytes());
    }

}

