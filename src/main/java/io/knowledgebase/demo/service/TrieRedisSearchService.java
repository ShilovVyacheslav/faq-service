package io.knowledgebase.demo.service;

import io.knowledgebase.demo.document.FaqDoc;
import io.knowledgebase.demo.dto.faq.FaqPreviewDto;

import java.util.List;

public interface TrieRedisSearchService {

    List<FaqPreviewDto> search(String query);

    void indexFaqDoc(FaqDoc faqDoc);

    void unindexFaqDoc(FaqDoc faqDoc);

    void sanitizeTrie();

}
