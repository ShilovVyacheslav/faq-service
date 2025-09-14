package io.knowledgebase.demo.service.jwt;

import io.knowledgebase.demo.security.SecurityUser;

import java.util.Set;

public interface JwtService {

    String generateAccessToken(SecurityUser securityUser);

    String extractUsername(String token);

    boolean isTokenValid(String token, SecurityUser securityUser);

    Long getCurrentUserId();

    Set<String> getCurrentUserRoles();

    String getUserName();
}
