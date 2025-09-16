package io.knowledgebase.demo.service.impl;

import io.knowledgebase.demo.document.FaqDoc;
import io.knowledgebase.demo.dto.faq.FaqPreviewDto;
import io.knowledgebase.demo.exception.FaqDocException;
import io.knowledgebase.demo.normalizer.KeywordNormalizer;
import io.knowledgebase.demo.service.TrieRedisSearchService;
import io.knowledgebase.demo.service.cache.FaqDocCacheService;
import io.knowledgebase.demo.service.trie.TrieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class TrieRedisSearchServiceImpl implements TrieRedisSearchService {

    private final TrieService trieService;
    private final FaqDocCacheService faqDocCacheService;
    private final KeywordNormalizer keywordNormalizer;

    @Override
    public List<FaqPreviewDto> search(String query) {

        List<String> processedTerms = processSearchQuery(query);

        List<Set<Long>> searchResults = performParallelSearch(processedTerms);

        Map<Long, Integer> weightedResults = calculateDocumentWeights(searchResults);

        return buildResponse(weightedResults);
    }

    @Override
    public void indexFaqDoc(FaqDoc faqDoc) {

        log.debug("Indexing FAQ doc with ID: {}", faqDoc.getId());

        faqDoc.getKeywords().stream()
                .map(keywordNormalizer::normalize)
                .filter(keywordNormalizer::isValid)
                .forEach(keyword -> trieService.insert(keyword, faqDoc.getId()));

        faqDocCacheService.cacheFaqDocument(faqDoc);

        log.debug("Successfully indexed FAQ doc with ID: {}", faqDoc.getId());
    }

    @Override
    public void unindexFaqDoc(FaqDoc faqDoc) {

        log.debug("Unindexing FAQ doc with ID: {}", faqDoc.getId());

        faqDoc.getKeywords().stream()
                .map(keywordNormalizer::normalize)
                .filter(keywordNormalizer::isValid)
                .forEach(keyword -> trieService.remove(keyword, faqDoc.getId()));

        faqDocCacheService.evictFaqDocument(faqDoc.getId());

        log.debug("Successfully unindexed FAQ doc with ID: {}", faqDoc.getId());
    }

    @Override
    public void sanitizeTrie() {
        trieService.cleanupOrphanedNodes();
    }

    private List<String> processSearchQuery(String query) {
        return Arrays.stream(query.split("\\s+"))
                .map(keywordNormalizer::normalize)
                .filter(keywordNormalizer::isValid)
                .toList();
    }

    private List<Set<Long>> performParallelSearch(List<String> searchTerms) {
        return searchTerms.parallelStream()
                .map(trieService::search)
                .toList();
    }

    private Map<Long, Integer> calculateDocumentWeights(List<Set<Long>> searchResults) {
        return searchResults.parallelStream()
                .flatMap(Set::stream)
                .collect(Collectors.groupingBy(
                        id -> id,
                        Collectors.summingInt(id -> 1)
                ));
    }

    private List<FaqPreviewDto> buildResponse(Map<Long, Integer> weightedResults) {
        return weightedResults.entrySet().parallelStream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(this::mapToFaqDocResponse)
                .filter(Objects::nonNull)
                .filter(this::isFaqActive)
                .toList();
    }

    private FaqPreviewDto mapToFaqDocResponse(Map.Entry<Long, Integer> entry) {
        try {
            return faqDocCacheService.readFaqDocument(entry.getKey());
        } catch (FaqDocException e) {
            return null;
        }
    }

    private boolean isFaqActive(FaqPreviewDto faqPreview) {
        return faqPreview != null && Boolean.TRUE.equals(faqPreview.getActive());
    }

}
