package io.knowledgebase.demo.normalizer;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class KeywordNormalizer {

    private static final Pattern NON_LETTER_PATTERN = Pattern.compile("[^\\p{L}*]");

    public String normalize(String term) {

        if (term == null) {
            return "";
        }

        String normalized = Normalizer.normalize(term, Normalizer.Form.NFKC);

        normalized = NON_LETTER_PATTERN.matcher(normalized).replaceAll("");

        if (normalized.endsWith("*")) {
            normalized = normalized.substring(0, normalized.length() - 1).replace("*", "") + "*";
        } else {
            normalized = normalized.replace("*", "");
        }

        normalized = normalized.toLowerCase(Locale.ROOT);

        return normalized;

    }

    public boolean isValid(String term) {
        return term != null && !term.trim().isEmpty();
    }

}
