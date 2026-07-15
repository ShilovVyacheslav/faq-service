# Trie + Redis Search — Load Test

## Setup

10,000 FAQ documents, 6-8 keywords each, drawn from a 2,000-wordlist (20% wildcard entries).

1,000 queries: 400 exact single-word, 400 wildcard-fallback (full words over a wildcarded stem), 100 handwritten multi-word phrases, 100 unindexed (no-match).

Cache is warmed while generating the documents, then explicitly cleared right before the measured phase. The Trie itself stays fully built — it's in-memory, unaffected by clearing Redis. Mongo and Redis are real containers via Testcontainers.

## Results

| Metric | Value |
|---|---|
| Overall average | 10.555 ms |
| p50 | 1.507 ms |
| p95 | 24.753 ms |
| p99 | 173.918 ms |
| Avg, cache miss | 25.499 ms (n=301) |
| Avg, cache hit | 5.172 ms (n=555) |
| Avg, empty result | 0.065 ms (n=144) |

Average and median diverge a lot here (10.5ms vs 1.5ms) — a handful of expensive queries pull the average up. p50 is the more honest number for typical cost; p99 (174ms) is the worst case.

Cache: 224,977 document reads, 215,386 hits — 95.7% hit rate. Classification: full_hit=555, partial=296, full_miss=5, empty_result=144.

| Category | Count | Avg latency (ms) | Avg docs matched |
|---|---|---|---|
| handwritten | 100 | 83.870 | 1965.0 |
| exact | 400 | 3.075 | 37.2 |
| wildcard_fallback | 400 | 2.326 | 34.0 |
| no_match | 100 | 0.077 | 0.0 |

## Handwritten queries are the outlier

25-35x slower than the other categories, and it's not the extra words — it's that natural phrases contain short, common terms ("do", "is", "my"). `TrieServiceImpl.insert()` adds the document ID to every node along the insertion path, not just the last one, so a two-letter prefix matches everything starting with it (`download`, `doubt`, `double`...). One common short term can pull in a fifth of the dataset — average docs matched jumps to 1965, versus ~35 for the other categories.

There's no minimum term length or stopword filtering before a term hits the Trie. Both are standard in production search engines for this exact reason.

## Running it

Test: `src/test/java/io/knowledgebase/demo/service/TrieRedisSearchLoadTest.java`, tagged `load-test`.

```
mvn test -Dgroups=load-test -Dsurefire.excludedGroups=
```

Needs Docker locally, or trigger the `Load Test` workflow in GitHub Actions.