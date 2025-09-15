package io.knowledgebase.demo.service;

import io.knowledgebase.demo.dto.faq.FaqCreateDto;
import io.knowledgebase.demo.dto.faq.FaqPreviewDto;
import io.knowledgebase.demo.dto.faq.FaqResponseDto;
import io.knowledgebase.demo.dto.faq.FaqUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FaqService {

    FaqResponseDto createFaq(FaqCreateDto faqCreateDto);

    FaqResponseDto updateFaq(Long id, FaqUpdateDto faqUpdateDto);

    void deleteFaq(Long id);

    Page<FaqResponseDto> readAllFaqs(Pageable pageable);

    FaqResponseDto readFaqById(Long id);

    List<FaqResponseDto> searchFaqByKeyWord(String key);

    List<FaqPreviewDto> searchFaqDocs(String query);

}
