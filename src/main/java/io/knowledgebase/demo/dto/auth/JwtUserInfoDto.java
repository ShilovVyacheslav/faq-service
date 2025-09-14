package io.knowledgebase.demo.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class JwtUserInfoDto {
    private Long id;
    private Set<String> roles;
}
