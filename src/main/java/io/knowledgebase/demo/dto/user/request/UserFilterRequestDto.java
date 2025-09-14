package io.knowledgebase.demo.dto.user.request;

import io.knowledgebase.demo.enums.Role;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterRequestDto {
    @Size(min = 1, max = 128)
    private String fullname;
    @Size(min = 1, max = 32)
    private String username;
    @Size(min = 1, max = 64)
    private String email;

    private Role role;
    private Boolean active;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdAtFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdAtTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate updatedAtFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate updatedAtTo;
}
