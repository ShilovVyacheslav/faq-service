package io.knowledgebase.demo.service.impl;

import io.knowledgebase.demo.document.FaqDoc;
import io.knowledgebase.demo.dto.faq.FaqPreviewDto;
import io.knowledgebase.demo.dto.faq.FaqUpdateDto;
import io.knowledgebase.demo.entity.Faq;
import io.knowledgebase.demo.exception.FaqDocException;
import io.knowledgebase.demo.mapper.FaqDocMapper;
import io.knowledgebase.demo.repository.FaqDocRepository;
import io.knowledgebase.demo.repository.FaqRepository;
import io.knowledgebase.demo.service.FaqDocService;
import io.knowledgebase.demo.service.TrieRedisSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class FaqDocServiceImpl implements FaqDocService {

    private final TrieRedisSearchService trieRedisSearchService;
    private final FaqDocRepository faqDocRepository;
    private final FaqRepository faqRepository;
    private final FaqDocMapper faqDocMapper;

    @Override
    public List<FaqPreviewDto> searchFaqDocs(String query) {

        log.debug("Search FAQ docs for query: '{}'", query);

        List<FaqPreviewDto> results = trieRedisSearchService.search(query);

        log.debug("Found {} results for query: '{}'", results.size(), query);

        return results;
    }

    @Override
    public Faq moveFaqToMongo(Faq faq) {
        createFaqDoc(faq);
        faq.setInMongo(true);
        return faqRepository.save(faq);
    }

    @Override
    public void createFaqDoc(Faq faq) {

        FaqDoc faqDoc = faqDocMapper.fromFaqToFaqDoc(faq);

        faqDocRepository.save(faqDoc);

        trieRedisSearchService.indexFaqDoc(faqDoc);

        log.debug("Successfully saved and indexed FAQ doc with ID: {}", faq.getId());
    }

    @Override
    public void updateFaqDoc(FaqUpdateDto faqUpdateDto, Long id) {

        log.info("Updating FAQ doc with ID: {}", id);

        FaqDoc faqDoc = faqDocRepository.findById(id).orElseThrow(() -> FaqDocException.faqDocNotFound(id));

        trieRedisSearchService.unindexFaqDoc(faqDoc);

        faqDocMapper.updateFromDto(faqUpdateDto, faqDoc);

        FaqDoc newFaqDoc = faqDocRepository.save(faqDoc);

        trieRedisSearchService.indexFaqDoc(newFaqDoc);

        trieRedisSearchService.sanitizeTrie();

        log.info("Successfully updated and indexed FAQ doc with ID: {}", id);
    }

    @Override
    public void deleteFaqDoc(Long id) {

        log.info("Deleting FAQ doc with ID: {}", id);

        FaqDoc faqDoc = faqDocRepository.findById(id).orElseThrow(() -> FaqDocException.faqDocNotFound(id));

        trieRedisSearchService.unindexFaqDoc(faqDoc);

        trieRedisSearchService.sanitizeTrie();

        faqDocRepository.delete(faqDoc);

        log.info("Successfully deleted FAQ doc with ID: {}", id);
    }

}
