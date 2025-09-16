package io.knowledgebase.demo.dto.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.knowledgebase.demo.dto.user.request.UserFilterRequestDto;
import io.knowledgebase.demo.enums.Role;
import jakarta.validation.ConstraintViolation;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserFilterRequestDtoTest extends UserDtoTestBase {

    private static final LocalDate VALID_DATE = LocalDate.now();

    private UserFilterRequestDto userFilterRequestDto;

    @BeforeEach
    void initUserFilterRequestDto() {
        userFilterRequestDto = new UserFilterRequestDto(
                VALID_FULLNAME,
                VALID_USERNAME,
                VALID_EMAIL,
                VALID_ROLE,
                VALID_ACTIVE,
                VALID_DATE,
                VALID_DATE,
                VALID_DATE,
                VALID_DATE
        );
    }

    @Test
    @Order(1)
    @DisplayName("[1] Test valid UserFilterRequestDto")
    void testValidUserFilterRequestDto_ShouldPassValidation() {
        Set<ConstraintViolation<UserFilterRequestDto>> violations = validator.validate(userFilterRequestDto);
        assertTrue(violations.isEmpty(), "There should be no violations");
    }

    @ParameterizedTest
    @Order(2)
    @DisplayName("[2] Test invalid fullname validation")
    @MethodSource("provideInvalidFullnames")
    void testInvalidFullnameValidation(String fullname) {
        assertInvalidFieldValidation("fullname", dto -> dto.setFullname(fullname));
    }

    @ParameterizedTest
    @Order(3)
    @DisplayName("[3] Test valid fullname validation")
    @NullSource
    @MethodSource("provideValidFullnames")
    void testValidFullnameValidation(String fullname) {
        assertValidFieldValidation(dto -> dto.setFullname(fullname));
    }

    @ParameterizedTest
    @Order(4)
    @DisplayName("[4] Test invalid username validation")
    @MethodSource("provideInvalidUsernames")
    void testInvalidUsernameValidation(String username) {
        assertInvalidFieldValidation("username", dto -> dto.setUsername(username));
    }

    @ParameterizedTest
    @Order(5)
    @DisplayName("[5] Test valid username validation")
    @NullSource
    @MethodSource("provideValidUsernames")
    void testValidUsernameValidation(String username) {
        assertValidFieldValidation(dto -> dto.setUsername(username));
    }

    @ParameterizedTest
    @Order(6)
    @DisplayName("[6] Test invalid email validation")
    @MethodSource("provideInvalidEmails")
    void testInvalidEmailValidation(String email) {
        assertInvalidFieldValidation("email", dto -> dto.setEmail(email));
    }

    @ParameterizedTest
    @Order(7)
    @DisplayName("[7] Test valid email validation")
    @NullSource
    @MethodSource("provideValidEmails")
    void testValidEmailValidation(String email) {
        assertValidFieldValidation(dto -> dto.setEmail(email));
    }

    @ParameterizedTest
    @Order(8)
    @DisplayName("[8] Test invalid role validation")
    @MethodSource("provideInvalidRoles")
    void testInvalidRoleValidation(String roleStr) {
        assertThatThrownBy(() -> {
            Role role = Role.fromString(roleStr);
            assertInvalidFieldValidation("role", dto -> dto.setRole(role));
        }).isInstanceOfAny(IllegalArgumentException.class, NullPointerException.class);
    }

    @ParameterizedTest
    @Order(9)
    @DisplayName("[9] Test valid role validation")
    @NullSource
    @MethodSource("provideValidRoles")
    void testValidRoleValidation(String roleStr) {
        assertDoesNotThrow(
                () -> {
                    Role role = roleStr != null ? Role.fromString(roleStr) : null;
                    assertValidFieldValidation(dto -> dto.setRole(role));
                }
        );
    }

    @ParameterizedTest
    @Order(10)
    @DisplayName("[10] Test invalid active validation")
    @MethodSource("provideInvalidActives")
    void testInvalidActiveValidation(Object active) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = String.format("{\"active\": %s}", active instanceof String ? "\"" + active + "\"" : active);
        assertThatThrownBy(() -> objectMapper.readValue(json, UserFilterRequestDto.class))
                .isInstanceOf(InvalidFormatException.class);
    }

    @ParameterizedTest
    @Order(11)
    @DisplayName("[11] Test valid active validation")
    @NullSource
    @MethodSource("provideValidActives")
    void testValidActiveValidation(Boolean active) {
        assertValidFieldValidation(dto -> dto.setActive(active));
    }

    @ParameterizedTest
    @Order(12)
    @DisplayName("[12] Test invalid LocalDate validation")
    @MethodSource("provideInvalidLocalDate")
    void testInvalidLocalDateValidation(String localDate) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = String.format("{\"createdAtFrom\": \"%s\"}", localDate);
        assertThatThrownBy(() -> objectMapper.readValue(json, UserFilterRequestDto.class))
                .isInstanceOf(com.fasterxml.jackson.databind.exc.InvalidFormatException.class);
    }

    @ParameterizedTest
    @Order(13)
    @DisplayName("[13] Test valid LocalDate validation")
    @NullSource
    @MethodSource("provideValidLocalDate")
    void testValidLocalDateValidation(String localDate) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = localDate != null
                ? String.format("{\"createdAtFrom\": \"%s\"}", localDate)
                : "{\"createdAtFrom\": null}";
        UserFilterRequestDto dto = assertDoesNotThrow(
                () -> objectMapper.readValue(json, UserFilterRequestDto.class)
        );
        assertThat(dto.getCreatedAtFrom())
                .isEqualTo(localDate != null ? LocalDate.parse(localDate) : null);
    }

    @Test
    @Order(14)
    @DisplayName("[14] Test builder pattern")
    void testBuilderPattern() {
        UserFilterRequestDto builtDto = UserFilterRequestDto.builder()
                .fullname(VALID_FULLNAME)
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .role(VALID_ROLE)
                .active(VALID_ACTIVE)
                .createdAtFrom(VALID_DATE)
                .createdAtTo(VALID_DATE)
                .updatedAtFrom(VALID_DATE)
                .updatedAtTo(VALID_DATE)
                .build();

        assertThat(builtDto).isNotNull();
        assertEquals(builtDto.getFullname(), VALID_FULLNAME);
        assertEquals(builtDto.getUsername(), VALID_USERNAME);
        assertEquals(builtDto.getEmail(), VALID_EMAIL);
        assertEquals(builtDto.getRole(), VALID_ROLE);
        assertEquals(builtDto.getActive(), VALID_ACTIVE);
        assertEquals(builtDto.getCreatedAtFrom(), VALID_DATE);
        assertEquals(builtDto.getCreatedAtTo(), VALID_DATE);
        assertEquals(builtDto.getUpdatedAtFrom(), VALID_DATE);
        assertEquals(builtDto.getUpdatedAtTo(), VALID_DATE);
    }

    private void assertInvalidFieldValidation(String fieldName, Consumer<UserFilterRequestDto> setter) {
        setter.accept(userFilterRequestDto);
        Set<ConstraintViolation<UserFilterRequestDto>> violations = validator.validate(userFilterRequestDto);
        AssertionsForInterfaceTypes.assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(fieldName));
    }

    private void assertValidFieldValidation(Consumer<UserFilterRequestDto> setter) {
        setter.accept(userFilterRequestDto);
        assertTrue(validator.validate(userFilterRequestDto).isEmpty());
    }

    protected static Stream<Arguments> provideInvalidFullnames() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("A".repeat(129))
        );
    }

    protected static Stream<Arguments> provideValidFullnames() {
        return Stream.of(
                Arguments.of("A"),
                Arguments.of(".-'"),
                Arguments.of("John Doe"),
                Arguments.of("Anna-Marie Smith"),
                Arguments.of("O'Connor James"),
                Arguments.of("Mary Jane Watson"),
                Arguments.of("J.R.R.Tolkien"),
                Arguments.of("Alex"),
                Arguments.of("Christopher Jonathan Richardson"),
                Arguments.of("A".repeat(128))
        );
    }

    protected static Stream<Arguments> provideInvalidUsernames() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("U".repeat(33))
        );
    }

    protected static Stream<Arguments> provideValidUsernames() {
        return Stream.of(
                Arguments.of("U"),
                Arguments.of("user123"),
                Arguments.of("john_doe"),
                Arguments.of("anna.smith"),
                Arguments.of("Mark-OConnor"),
                Arguments.of("alex99"),
                Arguments.of("user-name_123"),
                Arguments.of("john'doe"),
                Arguments.of("U".repeat(32))
        );
    }

    protected static Stream<Arguments> provideInvalidEmails() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("E".repeat(64) + "@gmail.com")
        );
    }

    protected static Stream<Arguments> provideValidEmails() {
        return Stream.of(
                Arguments.of("E"),
                Arguments.of("test@example.com"),
                Arguments.of("john.doe@gmail.com"),
                Arguments.of("anna_smith123@yahoo.co.uk"),
                Arguments.of("user.name+tag@sub.domain.com"),
                Arguments.of("a".repeat(58) + "@g.com")
        );
    }

    protected static Stream<Arguments> provideInvalidRoles() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of("manager"),
                Arguments.of("superadmin"),
                Arguments.of("root"),
                Arguments.of("123"),
                Arguments.of("user admin"),
                Arguments.of("_admin"),
                Arguments.of("admin_"),
                Arguments.of("admin".repeat(4))
        );
    }

    protected static Stream<Arguments> provideValidRoles() {
        return Stream.of(
                Arguments.of("USER"),
                Arguments.of("EXPERT"),
                Arguments.of("ADMIN"),
                Arguments.of("user"),
                Arguments.of("expert"),
                Arguments.of("admin"),
                Arguments.of("User"),
                Arguments.of("Expert"),
                Arguments.of("Admin"),
                Arguments.of("uSeR"),
                Arguments.of("eXpErT"),
                Arguments.of("aDmIn")
        );
    }

    protected static Stream<Arguments> provideInvalidActives() {
        return Stream.of(
                Arguments.of("1"),
                Arguments.of("0"),
                Arguments.of("2"),
                Arguments.of("abc"),
                Arguments.of("Yes"),
                Arguments.of("123 ")
        );
    }

    protected static Stream<Arguments> provideValidActives() {
        return Stream.of(
                Arguments.of(true),
                Arguments.of(false)
        );
    }

    protected static Stream<Arguments> provideInvalidLocalDate() {
        return Stream.of(
                Arguments.of("2025-02-30"),
                Arguments.of("16/08/2025"),
                Arguments.of("abc"),
                Arguments.of("2025-13-01")
        );
    }

    protected static Stream<Arguments> provideValidLocalDate() {
        return Stream.of(
                Arguments.of("2025-08-16"),
                Arguments.of("2000-01-01"),
                Arguments.of("1995-06-15"),
                Arguments.of("2023-12-31")
        );
    }

}
