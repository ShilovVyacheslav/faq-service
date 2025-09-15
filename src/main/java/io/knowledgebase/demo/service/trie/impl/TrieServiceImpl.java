package io.knowledgebase.demo.service.trie.impl;

import io.knowledgebase.demo.normalizer.KeywordNormalizer;
import io.knowledgebase.demo.repository.FaqDocRepository;
import io.knowledgebase.demo.service.trie.TrieNode;
import io.knowledgebase.demo.service.trie.TrieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class TrieServiceImpl implements TrieService {

    private final TrieNode root = new TrieNode();
    private final FaqDocRepository faqDocRepository;
    private final KeywordNormalizer keywordNormalizer;

    @Transactional(readOnly = true)
    @EventListener(ApplicationReadyEvent.class)
    @Override
    public void init() {

        log.info("Initializing TrieService with FAQ documents...");

        int processedCount = faqDocRepository.findAll()
                .stream().mapToInt(faqDoc -> {
                    faqDoc.getKeywords().forEach(keyword ->
                            insert(keyword, faqDoc.getId()));
                    return 1;
                })
                .sum();

        log.info("Initialized TrieService with {} documents", processedCount);
    }

    @Override
    public void insert(String word, Long faqId) {

        String normalizedWord = keywordNormalizer.normalize(word);
        if (!keywordNormalizer.isValid(normalizedWord)) {
            return;
        }
        TrieNode current = root;
        int n = normalizedWord.length();
        for (int i = 0; i < n; ++i) {
            current = current.getChildren().computeIfAbsent(
                    normalizedWord.charAt(i), k -> new TrieNode()
            );
            current.getFaqIds().add(faqId);
        }

    }

    @Override
    public Set<Long> search(String word) {

        String normalizedWord = keywordNormalizer.normalize(word);
        if (!keywordNormalizer.isValid(normalizedWord)) {
            return Collections.emptySet();
        }
        TrieNode current = root, next;
        int n = normalizedWord.length();
        for (int i = 0; i < n; ++i) {
            next = current.getChildren().get(normalizedWord.charAt(i));
            if (next == null) {
                TrieNode starNode = current.getChildren().get('*');
                if (starNode == null) {
                    return Collections.emptySet();
                }
                return  Collections.unmodifiableSet(starNode.getFaqIds());
            }
            current = next;
        }

        return Collections.unmodifiableSet(current.getFaqIds());
    }

    @Override
    public void remove(String word, Long faqId) {

        log.trace("Removing word '{}' for FAQ ID: {}", word, faqId);

        String normalizedWord = keywordNormalizer.normalize(word);
        if (!keywordNormalizer.isValid(normalizedWord)) {
            return;
        }
        TrieNode current = root;
        int n = normalizedWord.length();
        for (int i = 0; i < n; ++i) {
            current = current.getChildren().get(normalizedWord.charAt(i));
            if (current == null) {
                return;
            }
            current.getFaqIds().remove(faqId);
        }

        log.trace("Successfully removed word '{}' for FAQ ID: {}", normalizedWord, faqId);
    }

    @Override
    public void cleanupOrphanedNodes() {
        log.debug("Starting trie cleanup...");
        cleanupNode(root);
        log.debug("Trie cleanup completed");
    }

    private boolean cleanupNode(TrieNode node) {
        if (node == null) {
            return true;
        }
        node.getChildren().entrySet().removeIf(entry -> {
            TrieNode childNode = entry.getValue();
            boolean shouldRemove = cleanupNode(childNode);
            return shouldRemove && childNode.getFaqIds().isEmpty();
        });
        return node.getChildren().isEmpty() && node.getFaqIds().isEmpty();
    }

}
