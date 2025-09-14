package io.knowledgebase.demo.common.util;

import io.knowledgebase.demo.dto.auth.LoginRequestDto;
import io.knowledgebase.demo.exception.AuthException;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@UtilityClass
public class AuthUtil {

    public static LoginRequestDto base64ToLoginRequestDto(String authorization) {

        if (authorization == null || !authorization.startsWith("Basic ")) {
            throw AuthException.invalidCredentials();
        }

        String base64Credentials = authorization.substring("Basic ".length());

        String decoded = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

        String[] credentials = decoded.split(":");

        return LoginRequestDto.builder()
                .username(credentials[0])
                .password(credentials[1])
                .build();
    }
}
