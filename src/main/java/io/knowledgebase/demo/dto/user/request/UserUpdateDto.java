package io.knowledgebase.demo.dto.user.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.knowledgebase.demo.common.deserializer.StrictBooleanDeserializer;
import io.knowledgebase.demo.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.knowledgebase.demo.common.validation.ValidationPatterns.FULLNAME_REGEXP;
import static io.knowledgebase.demo.common.validation.ValidationPatterns.PASSWORD_REGEXP;
import static io.knowledgebase.demo.common.validation.ValidationPatterns.USERNAME_REGEXP;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    @Size(min = 2, max = 128)
    @Pattern(regexp = FULLNAME_REGEXP)
    private String fullname;

    @Size(min = 2, max = 32)
    @Pattern(regexp = USERNAME_REGEXP)
    private String username;

    @Email
    @Size(min = 6, max = 64)
    private String email;

    @Size(min = 8, max = 32)
    @Pattern(regexp = PASSWORD_REGEXP)
    private String password;

    private Role role;

    @JsonDeserialize(using = StrictBooleanDeserializer.class)
    private Boolean active;
}
