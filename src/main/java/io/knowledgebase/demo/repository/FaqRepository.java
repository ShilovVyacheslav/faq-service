package io.knowledgebase.demo.repository;

import io.knowledgebase.demo.entity.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long>, JpaSpecificationExecutor<Faq> {

    @EntityGraph(attributePaths = {"createdBy"})
    @NonNull
    Page<Faq> findAll(Specification<Faq> spec, @NonNull Pageable pageable);

    @Query(value = """
            SELECT * FROM faq
            WHERE search_vector @@ to_tsquery('russian', :tsquery)
            """, nativeQuery = true)
    List<Faq> searchByTsQuery(@Param("tsquery") String tsquery);

    Faq getFaqByQuestionIgnoreCaseAndAnswerIgnoreCase(String question, String answer);

    List<Faq> findByInMongoFalse();

}
