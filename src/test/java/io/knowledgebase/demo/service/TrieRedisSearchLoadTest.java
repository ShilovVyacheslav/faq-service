package io.knowledgebase.demo.service;

import io.knowledgebase.demo.document.FaqDoc;
import io.knowledgebase.demo.dto.faq.FaqPreviewDto;
import io.knowledgebase.demo.normalizer.KeywordNormalizer;
import io.knowledgebase.demo.repository.FaqDocRepository;
import io.knowledgebase.demo.service.trie.TrieService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@Tag("load-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrieRedisSearchLoadTest {

    private static final int DOC_COUNT = 10_000;
    private static final int MIN_KEYWORDS_PER_DOC = 6;
    private static final int MAX_KEYWORDS_PER_DOC = 8;
    private static final int SEED_BATCH_SIZE = 1000;

    @Container
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:6-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", mongo::getHost);
        registry.add("spring.data.mongodb.port", () -> mongo.getMappedPort(27017));
        registry.add("spring.data.mongodb.database", () -> "faq-service-loadtest");
        registry.add("spring.data.mongodb.username", () -> "");
        registry.add("spring.data.mongodb.password", () -> "");

        registry.add("spring.data.redis.enabled", () -> "true");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");
        registry.add("spring.data.redis.flush-on-startup", () -> "false");

        registry.add("scheduler.faq-sync.enabled", () -> "false");
    }

    @Autowired
    private FaqDocRepository faqDocRepository;

    @Autowired
    private TrieRedisSearchService trieRedisSearchService;

    @Autowired
    private TrieService trieService;

    @Autowired
    private KeywordNormalizer keywordNormalizer;

    @Autowired
    private CacheManager cacheManager;

    private List<String> plainCorpus;
    private List<String> wildcardCorpus;
    private List<QueryCase> queries;
    private Cache faqDocsCache;

    private record QueryCase(String category, String text) {
    }

    private record QueryResult(
            String category,
            String text,
            long elapsedNanos,
            int touchedDocs,
            int cachedBeforeCall,
            int resultCount
    ) {
        boolean isFullHit() {
            return touchedDocs > 0 && cachedBeforeCall == touchedDocs;
        }

        boolean isFullMiss() {
            return touchedDocs > 0 && cachedBeforeCall == 0;
        }

        boolean isPartial() {
            return touchedDocs > 0 && cachedBeforeCall > 0 && cachedBeforeCall < touchedDocs;
        }

        boolean isEmpty() {
            return touchedDocs == 0;
        }
    }

    @BeforeAll
    void setUp() throws IOException {
        List<String> wordlist = readLines("load-test-wordlist.txt");
        plainCorpus = wordlist.stream().filter(w -> !w.endsWith("*")).toList();
        wildcardCorpus = wordlist.stream().filter(w -> w.endsWith("*")).toList();

        queries = readLines("search-queries.tsv").stream()
                .map(line -> {
                    String[] parts = line.split("\t", 2);
                    return new QueryCase(parts[0], parts[1]);
                })
                .toList();

        assertThat(queries).hasSize(1000);

        System.out.println("Seeding " + DOC_COUNT + " FAQ documents...");
        seedFaqDocuments();
        System.out.println("Seeding complete.");

        Cache cache = cacheManager.getCache("faqDocs");
        assertThat(cache).as("faqDocs cache must be active (spring.data.redis.enabled=true)").isNotNull();
        cache.clear();
        faqDocsCache = cache;
        System.out.println("faqDocs cache cleared - starting from a cold cache.");
    }

    private void seedFaqDocuments() {
        Random random = new Random(2024);
        List<String> combined = new ArrayList<>(plainCorpus);
        combined.addAll(wildcardCorpus);

        List<FaqDoc> batch = new ArrayList<>(SEED_BATCH_SIZE);
        for (long id = 1; id <= DOC_COUNT; id++) {
            int keywordCount = MIN_KEYWORDS_PER_DOC
                    + random.nextInt(MAX_KEYWORDS_PER_DOC - MIN_KEYWORDS_PER_DOC + 1);
            Set<String> keywordSet = new LinkedHashSet<>();
            while (keywordSet.size() < keywordCount) {
                keywordSet.add(combined.get(random.nextInt(combined.size())));
            }
            List<String> keywordList = new ArrayList<>(keywordSet);

            FaqDoc doc = FaqDoc.builder()
                    .id(id)
                    .question(buildQuestion(keywordList, random))
                    .answer(buildAnswer(keywordList))
                    .keywords(keywordList)
                    .active(true)
                    .build();

            batch.add(doc);
            if (batch.size() == SEED_BATCH_SIZE) {
                flushSeedBatch(batch);
                if (id % 2000 == 0) {
                    System.out.println("  seeded " + id + " / " + DOC_COUNT);
                }
            }
        }
        if (!batch.isEmpty()) {
            flushSeedBatch(batch);
        }
    }

    private void flushSeedBatch(List<FaqDoc> batch) {
        faqDocRepository.saveAll(batch);
        batch.forEach(trieRedisSearchService::indexFaqDoc);
        batch.clear();
    }

    private static final String[] QUESTION_TEMPLATES = {
            "How do I configure %s for %s?",
            "What is the recommended way to handle %s during %s?",
            "Why is my %s not working with %s?",
            "Where can I find settings related to %s and %s?",
            "Is %s compatible with %s?",
            "What happens to %s after %s?",
            "How can I troubleshoot %s issues involving %s?",
            "What is the difference between %s and %s?",
    };

    private String buildQuestion(List<String> keywords, Random random) {
        String template = QUESTION_TEMPLATES[random.nextInt(QUESTION_TEMPLATES.length)];
        String a = displayForm(keywords.getFirst());
        String b = displayForm(keywords.get(Math.min(1, keywords.size() - 1)));
        return String.format(Locale.ROOT, template, a, b);
    }

    private String buildAnswer(List<String> keywords) {
        String joined = keywords.stream().map(this::displayForm).collect(Collectors.joining(", "));
        return "This article covers the following topics: " + joined
                + ". Follow the steps in this guide to resolve related issues.";
    }

    private String displayForm(String keyword) {
        return keyword.endsWith("*") ? keyword.substring(0, keyword.length() - 1) : keyword;
    }

    private List<String> readLines(String resourceName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(resourceName).getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().filter(l -> !l.isBlank()).toList();
        }
    }

    @Test
    void searchLoadTest() throws IOException {

        List<QueryResult> results = new ArrayList<>(queries.size());

        for (QueryCase query : queries) {
            Set<Long> touchedIds = resolveTouchedDocIds(query.text());
            int cachedBefore = countCached(touchedIds);

            long start = System.nanoTime();
            List<FaqPreviewDto> searchResults = trieRedisSearchService.search(query.text());
            long elapsed = System.nanoTime() - start;

            results.add(new QueryResult(
                    query.category(),
                    query.text(),
                    elapsed,
                    touchedIds.size(),
                    cachedBefore,
                    searchResults.size()
            ));
        }

        LoadTestReport report = LoadTestReport.from(results);
        report.printTo();
        report.writeMarkdown(Paths.get("target", "load-test-report.md"));

        assertThat(results).hasSize(1000);
        assertThat(report.fullHitCount + report.fullMissCount + report.partialCount + report.emptyCount)
                .isEqualTo(1000);
        assertThat(report.fullHitCount)
                .as("expected at least some cache hits given repeated query terms")
                .isGreaterThan(0);
        assertThat(report.fullMissCount + report.partialCount)
                .as("expected at least some cache misses on a freshly cleared cache")
                .isGreaterThan(0);

        long noMatchWithResults = results.stream()
                .filter(r -> r.category().equals("no_match") && r.resultCount() > 0)
                .count();
        assertThat(noMatchWithResults)
                .as("no_match queries are built from words outside the corpus and should return nothing")
                .isZero();
    }

    private Set<Long> resolveTouchedDocIds(String query) {
        Set<Long> touched = new HashSet<>();
        for (String rawTerm : query.split("\\s+")) {
            String normalized = keywordNormalizer.normalize(rawTerm);
            if (!keywordNormalizer.isValid(normalized)) {
                continue;
            }
            touched.addAll(trieService.search(normalized));
        }
        return touched;
    }

    private int countCached(Set<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            if (faqDocsCache.get(id.toString()) != null) {
                count++;
            }
        }
        return count;
    }

    private static final class LoadTestReport {

        int fullHitCount;
        int fullMissCount;
        int partialCount;
        int emptyCount;

        double overallAvgMs;
        double p50Ms;
        double p95Ms;
        double p99Ms;

        double avgMsWithMisses;
        double avgMsNoMisses;
        double avgMsEmptyResult;

        long totalDocReads;
        long totalCacheHitReads;
        long totalCacheMissReads;

        List<QueryResult> raw;

        static LoadTestReport from(List<QueryResult> results) {
            LoadTestReport r = new LoadTestReport();
            r.raw = results;

            List<Long> allNanos = new ArrayList<>();
            List<Long> missNanos = new ArrayList<>();
            List<Long> hitNanos = new ArrayList<>();
            List<Long> emptyNanos = new ArrayList<>();

            for (QueryResult qr : results) {
                allNanos.add(qr.elapsedNanos());

                if (qr.isEmpty()) {
                    r.emptyCount++;
                    emptyNanos.add(qr.elapsedNanos());
                    continue;
                }
                if (qr.isFullHit()) {
                    r.fullHitCount++;
                    hitNanos.add(qr.elapsedNanos());
                } else if (qr.isFullMiss()) {
                    r.fullMissCount++;
                    missNanos.add(qr.elapsedNanos());
                } else if (qr.isPartial()) {
                    r.partialCount++;
                    missNanos.add(qr.elapsedNanos());
                }

                r.totalDocReads += qr.touchedDocs();
                r.totalCacheHitReads += qr.cachedBeforeCall();
                r.totalCacheMissReads += (qr.touchedDocs() - qr.cachedBeforeCall());
            }

            List<Long> sortedAll = allNanos.stream().sorted().toList();
            r.overallAvgMs = avgMs(allNanos);
            r.p50Ms = percentileMs(sortedAll, 0.50);
            r.p95Ms = percentileMs(sortedAll, 0.95);
            r.p99Ms = percentileMs(sortedAll, 0.99);

            r.avgMsWithMisses = avgMs(missNanos);
            r.avgMsNoMisses = avgMs(hitNanos);
            r.avgMsEmptyResult = avgMs(emptyNanos);

            return r;
        }

        private static double avgMs(List<Long> nanos) {
            if (nanos.isEmpty()) {
                return 0.0;
            }
            double sum = 0;
            for (long n : nanos) {
                sum += n;
            }
            return (sum / nanos.size()) / 1_000_000.0;
        }

        private static double percentileMs(List<Long> sortedNanos, double p) {
            if (sortedNanos.isEmpty()) {
                return 0.0;
            }
            int index = (int) Math.ceil(p * sortedNanos.size()) - 1;
            index = Math.max(0, Math.min(index, sortedNanos.size() - 1));
            return sortedNanos.get(index) / 1_000_000.0;
        }

        void printTo() {
            System.out.println();
            System.out.println("===== Trie + Redis search load test: 1000 queries over a 10,000-doc corpus =====");
            System.out.printf(Locale.ROOT, "Overall avg latency:          %.3f ms%n", overallAvgMs);
            System.out.printf(Locale.ROOT, "p50 / p95 / p99 latency:      %.3f / %.3f / %.3f ms%n", p50Ms, p95Ms, p99Ms);
            System.out.println();
            System.out.printf(Locale.ROOT, "Avg latency, cache miss (partial or full): %.3f ms  (n=%d)%n",
                    avgMsWithMisses, fullMissCount + partialCount);
            System.out.printf(Locale.ROOT, "Avg latency, full cache hit:                %.3f ms  (n=%d)%n",
                    avgMsNoMisses, fullHitCount);
            System.out.printf(Locale.ROOT, "Avg latency, empty result (no docs touched): %.3f ms  (n=%d)%n",
                    avgMsEmptyResult, emptyCount);
            System.out.println();
            System.out.printf(Locale.ROOT, "Query classification: full_hit=%d  partial=%d  full_miss=%d  empty=%d%n",
                    fullHitCount, partialCount, fullMissCount, emptyCount);
            System.out.printf(Locale.ROOT, "Document-level cache hit rate: %d / %d reads (%.1f%%)%n",
                    totalCacheHitReads, totalDocReads,
                    totalDocReads == 0 ? 0.0 : 100.0 * totalCacheHitReads / totalDocReads);
            System.out.println("===================================================================================");
            System.out.println();
        }

        void writeMarkdown(Path path) throws IOException {
            Files.createDirectories(path.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("# Trie + Redis Search Load Test Report\n\n");
            sb.append("- Corpus: 10,000 FAQ documents, 6-8 keywords each, drawn from a 2000-word corpus ")
                    .append("(20% wildcard/prefix entries)\n");
            sb.append("- Queries: 1000 total (100 handwritten, 400 exact, 400 wildcard-fallback, 100 no-match)\n");
            sb.append("- Cache: warmed during seeding, then explicitly cleared before the measured phase\n\n");

            sb.append("## Latency\n\n");
            sb.append("| Metric | Value |\n|---|---|\n");
            sb.append(String.format(Locale.ROOT, "| Overall average | %.3f ms |%n", overallAvgMs));
            sb.append(String.format(Locale.ROOT, "| p50 | %.3f ms |%n", p50Ms));
            sb.append(String.format(Locale.ROOT, "| p95 | %.3f ms |%n", p95Ms));
            sb.append(String.format(Locale.ROOT, "| p99 | %.3f ms |%n", p99Ms));
            sb.append(String.format(Locale.ROOT, "| Avg, cache miss (partial or full) | %.3f ms (n=%d) |%n",
                    avgMsWithMisses, fullMissCount + partialCount));
            sb.append(String.format(Locale.ROOT, "| Avg, full cache hit | %.3f ms (n=%d) |%n",
                    avgMsNoMisses, fullHitCount));
            sb.append(String.format(Locale.ROOT, "| Avg, empty result | %.3f ms (n=%d) |%n",
                    avgMsEmptyResult, emptyCount));

            sb.append("\n## Cache\n\n");
            sb.append(String.format(Locale.ROOT,
                    "- Document-level reads: %d, of which %d were cache hits (%.1f%%) and %d were cache misses%n",
                    totalDocReads, totalCacheHitReads,
                    totalDocReads == 0 ? 0.0 : 100.0 * totalCacheHitReads / totalDocReads,
                    totalCacheMissReads));
            sb.append(String.format(Locale.ROOT,
                    "- Query classification: full_hit=%d, partial=%d, full_miss=%d, empty_result=%d%n",
                    fullHitCount, partialCount, fullMissCount, emptyCount));

            sb.append("\n## By query category\n\n");
            sb.append("| Category | Count | Avg latency (ms) | Avg docs touched |\n|---|---|---|---|\n");
            for (String category : List.of("handwritten", "exact", "wildcard_fallback", "no_match")) {
                List<QueryResult> subset = raw.stream().filter(r -> r.category().equals(category)).toList();
                double avg = avgMs(subset.stream().map(QueryResult::elapsedNanos).toList());
                double avgDocs = subset.isEmpty() ? 0.0
                        : subset.stream().mapToInt(QueryResult::touchedDocs).average().orElse(0.0);
                sb.append(String.format(Locale.ROOT, "| %s | %d | %.3f | %.1f |%n",
                        category, subset.size(), avg, avgDocs));
            }

            Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
            System.out.println("Full report written to " + path.toAbsolutePath());
        }
    }
}
