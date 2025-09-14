package io.knowledgebase.demo.service.jwt;

import io.knowledgebase.demo.dto.auth.JwtUserInfoDto;
import io.knowledgebase.demo.dto.auth.LoginResponseDto;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthService {

    LoginResponseDto login(String authorization);

    LoginResponseDto refreshToken(Jwt jwt);

    JwtUserInfoDto getInfoByJwt();

}
