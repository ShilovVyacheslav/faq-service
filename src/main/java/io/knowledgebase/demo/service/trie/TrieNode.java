package io.knowledgebase.demo.service.trie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrieNode {
    @Builder.Default
    private Map<Character, TrieNode> children = new ConcurrentHashMap<>();
    @Builder.Default
    private Set<Long> faqIds = ConcurrentHashMap.newKeySet();
}
