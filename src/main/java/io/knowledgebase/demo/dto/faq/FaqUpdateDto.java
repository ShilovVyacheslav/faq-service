package io.knowledgebase.demo.dto.faq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FaqUpdateDto {
    private String question;
    private String answer;
    private List<String> keywords;
    private Boolean active;
}
