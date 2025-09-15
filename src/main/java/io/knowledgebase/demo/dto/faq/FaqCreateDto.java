package io.knowledgebase.demo.dto.faq;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FaqCreateDto {
    @NotBlank
    @Size(max = 256)
    private String question;

    @NotBlank
    private String answer;

    @NotNull
    private List<@NotBlank String> keywords;
}
