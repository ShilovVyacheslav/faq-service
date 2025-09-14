package io.knowledgebase.demo.config.props;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.security.jwt")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtProps {
    String secretKey;
    long accessExpiration;
    long refreshExpiration;
}
