package io.knowledgebase.demo.service.cache.impl;

import io.knowledgebase.demo.document.FaqDoc;
import io.knowledgebase.demo.dto.faq.FaqPreviewDto;
import io.knowledgebase.demo.exception.FaqDocException;
import io.knowledgebase.demo.mapper.FaqDocMapper;
import io.knowledgebase.demo.repository.FaqDocRepository;
import io.knowledgebase.demo.service.cache.FaqDocCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FaqDocCacheServiceImpl implements FaqDocCacheService {

    private final FaqDocRepository faqDocRepository;
    private final FaqDocMapper faqDocMapper;

    @Override
    @Cacheable(value = "faqDocs", key = "#id.toString()")
    public FaqPreviewDto readFaqDocument(Long id) {
        return faqDocMapper.toResponseDto(
                faqDocRepository.findById(id).orElseThrow(() -> FaqDocException.faqDocNotFound(id))
        );
    }

    @Override
    @CacheEvict(value = "faqDocs", key = "#id.toString()")
    public void evictFaqDocument(Long id) {
        log.debug("Evicting FAQ doc from cache with ID: {}", id);
    }

    @Override
    @CachePut(value = "faqDocs", key = "#faqDoc.id.toString()")
    public FaqPreviewDto cacheFaqDocument(FaqDoc faqDoc) {
        log.debug("Caching FAQ doc with ID: {}", faqDoc.getId());
        return faqDocMapper.toResponseDto(faqDoc);
    }

}
