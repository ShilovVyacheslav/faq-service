package io.knowledgebase.demo.dto.faq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqPreviewDto {
    private Long id;
    private String question;
    private Boolean active;
}
