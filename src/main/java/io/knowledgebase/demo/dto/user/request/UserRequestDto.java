package io.knowledgebase.demo.dto.user.request;

import io.knowledgebase.demo.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class UserRequestDto {
    @NotBlank
    @Size(min = 2, max = 128)
    @Pattern(regexp = FULLNAME_REGEXP)
    private String fullname;

    @NotBlank
    @Size(min = 2, max = 32)
    @Pattern(regexp = USERNAME_REGEXP)
    private String username;

    @Email
    @NotBlank
    @Size(min = 6, max = 64)
    private String email;

    @NotBlank
    @Size(min = 8, max = 32)
    @Pattern(regexp = PASSWORD_REGEXP)
    private String password;

    @NotNull
    private Role role;
}
