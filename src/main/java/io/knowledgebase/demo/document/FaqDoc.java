package io.knowledgebase.demo.document;

import io.knowledgebase.demo.entity.User;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "FaqDocument")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqDoc {
    @Id
    private Long id;

    private String question;

    private String answer;

    private List<String> keywords;

    private User createdBy;

    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
