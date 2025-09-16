package io.knowledgebase.demo.normalizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeywordNormalizerTest {

    private KeywordNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new KeywordNormalizer();
    }

    @ParameterizedTest(name = "[{index}] Input: ''{0}''")
    @DisplayName("Normalize - null and empty input should return empty string")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "    ", "\t", "\n"})
    void normalize_ShouldReturnEmptyString_ForNullOrBlankInput(String input) {
        assertEquals("", normalizer.normalize(input));
    }

    @ParameterizedTest(name = "[{index}] Input: ''{0}'' -> Expected: ''{1}''")
    @DisplayName("Normalize  - should handle various normalization scenarious")
    @MethodSource("provideNormalizationTestCases")
    void normalize_ShouldReturnExpectedResult(String input, String expected) {
        assertEquals(expected, normalizer.normalize(input));
    }

    @ParameterizedTest(name = "[{index}] Input: ''{0}''")
    @DisplayName("isValid - should return false for invalid input")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void isValid_ShouldReturnFalse_ForInvalidInput(String input) {
        assertFalse(normalizer.isValid(input));
    }

    @ParameterizedTest(name = "[{index}] Input: ''{0}''")
    @DisplayName("isValid - should return true for valid input")
    @MethodSource("provideValidInputTestCases")
    void isValid_ShouldReturnTrue_ForValidInput(String input) {
        assertTrue(normalizer.isValid(input));
    }

    private static Stream<Arguments> provideNormalizationTestCases() {
        return Stream.of(
                Arguments.of("HELLO", "hello"),
                Arguments.of("Hello World!", "helloworld"),
                Arguments.of("Café_au*lait", "caféaulait"),
                Arguments.of("Naïve*", "naïve*"),
                Arguments.of("test*123", "test*"),
                Arguments.of("*start", "start"),
                Arguments.of("mid*dle", "middle"),
                Arguments.of("end*", "end*"),
                Arguments.of("multiple**", "multiple*"),
                Arguments.of("UPPER*CASE", "uppercase"),
                Arguments.of("with-hyphen", "withhyphen"),
                Arguments.of("with_underscore", "withunderscore"),
                Arguments.of("with.dots", "withdots"),
                Arguments.of("with spaces", "withspaces"),
                Arguments.of("123numbers", "numbers"),
                Arguments.of("!@#$%symbols", "symbols"),
                Arguments.of("café", "café"),
                Arguments.of("naïve", "naïve"),
                Arguments.of("straße", "straße"),
                Arguments.of("composite*test!123", "compositetest"),
                Arguments.of("mixed-CASE*", "mixedcase*"),
                Arguments.of("a*b*c*", "abc*"),
                Arguments.of("***test***", "test*"),
                Arguments.of("Octomobile 2.0", "octomobile")
        );
    }

    private static Stream<Arguments> provideValidInputTestCases() {
        return Stream.of(
                Arguments.of("a"),
                Arguments.of("hello"),
                Arguments.of("test*"),
                Arguments.of(" word "),
                Arguments.of("café"),
                Arguments.of("naïve"),
                Arguments.of("hello world"),
                Arguments.of("123"),
                Arguments.of("!@#"),
                Arguments.of("*"),
                Arguments.of("a*"),
                Arguments.of("multiple words with symbols!")
        );
    }

}
