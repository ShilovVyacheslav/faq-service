package io.knowledgebase.demo.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String token;
    @Builder.Default
    private final String tokenType = "Bearer";
    private long expiresIn;
}
