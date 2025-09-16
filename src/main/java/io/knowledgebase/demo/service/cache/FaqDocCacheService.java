package io.knowledgebase.demo.service.cache;

import io.knowledgebase.demo.document.FaqDoc;
import io.knowledgebase.demo.dto.faq.FaqPreviewDto;

public interface FaqDocCacheService {

    FaqPreviewDto readFaqDocument(Long id);

    void evictFaqDocument(Long id);

    FaqPreviewDto cacheFaqDocument(FaqDoc faqDoc);

}
