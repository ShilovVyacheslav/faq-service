package io.knowledgebase.demo.service.trie;

import java.util.Set;

public interface TrieService {

    void init();

    void insert(String word, Long faqId);

    Set<Long> search(String word);

    void remove(String word, Long faqId);

    void cleanupOrphanedNodes();

}
