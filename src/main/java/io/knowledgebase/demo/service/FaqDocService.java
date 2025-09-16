package io.knowledgebase.demo.service;

import io.knowledgebase.demo.dto.faq.FaqPreviewDto;
import io.knowledgebase.demo.dto.faq.FaqUpdateDto;
import io.knowledgebase.demo.entity.Faq;

import java.util.List;

public interface FaqDocService {

    List<FaqPreviewDto> searchFaqDocs(String query);

    Faq moveFaqToMongo(Faq faq);

    void createFaqDoc(Faq faq);

    void updateFaqDoc(FaqUpdateDto faqUpdateDto, Long id);

    void deleteFaqDoc(Long id);

}
