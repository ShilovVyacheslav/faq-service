package io.knowledgebase.demo.dto.user;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.knowledgebase.demo.dto.user.request.UserUpdateDto;
import io.knowledgebase.demo.enums.Role;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserUpdateDtoTest extends UserDtoTestBase {

    private UserUpdateDto userUpdateDto;

    @BeforeEach
    void initUserRequestDto() {
        userUpdateDto = new UserUpdateDto(
                VALID_FULLNAME,
                VALID_USERNAME,
                VALID_EMAIL,
                VALID_PASSWORD,
                VALID_ROLE,
                VALID_ACTIVE
        );
    }

    @Test
    @Order(1)
    @DisplayName("[1] Test valid UserUpdateDto")
    void testValidUserUpdateDto() {
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(userUpdateDto);
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
    @DisplayName("[8] Test invalid password validation")
    @MethodSource("provideInvalidPasswords")
    void testInvalidPasswordValidation(String password) {
        assertInvalidFieldValidation("password", dto -> dto.setPassword(password));
    }

    @ParameterizedTest
    @Order(9)
    @DisplayName("[9] Test valid password validation")
    @NullSource
    @MethodSource("provideValidPasswords")
    void testValidPasswordValidation(String password) {
        assertValidFieldValidation(dto -> dto.setPassword(password));
    }

    @ParameterizedTest
    @Order(10)
    @DisplayName("[10] Test invalid role validation")
    @MethodSource("provideInvalidRoles")
    void testInvalidRoleValidation(String roleStr) {
        assertThatThrownBy(() -> {
            Role role = Role.fromString(roleStr);
            assertInvalidFieldValidation("role", dto -> dto.setRole(role));
        }).isInstanceOfAny(IllegalArgumentException.class, NullPointerException.class);
    }

    @ParameterizedTest
    @Order(11)
    @DisplayName("[11] Test valid role validation")
    @MethodSource("provideValidRoles")
    void testValidRoleValidation(String roleStr) {
        assertDoesNotThrow(
                () -> {
                    Role role = Role.fromString(roleStr);
                    assertValidFieldValidation(dto -> dto.setRole(role));
                }
        );
    }

    @Test
    @Order(12)
    @DisplayName("[12] Test null role validation")
    void testNullRoleValidation() {
        userUpdateDto.setRole(null);
        assertTrue(validator.validate(userUpdateDto).isEmpty());
    }

    @ParameterizedTest
    @Order(13)
    @DisplayName("[13] Test invalid active validation")
    @MethodSource("provideInvalidActives")
    void testInvalidActiveValidation(Object active) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = String.format("{\"active\": %s}", active instanceof String ? "\"" + active + "\"" : active);
        assertThatThrownBy(() -> objectMapper.readValue(json, UserUpdateDto.class))
                .isInstanceOf(JsonMappingException.class)
                .cause()
                .isInstanceOf(JsonParseException.class);
    }

    @ParameterizedTest
    @Order(14)
    @DisplayName("[14] Test valid active validation")
    @NullSource
    @MethodSource("provideValidActives")
    void testValidActiveValidation(Boolean active) {
        assertValidFieldValidation(dto -> dto.setActive(active));
    }

    @Test
    @Order(15)
    @DisplayName("[15] Test builder pattern")
    void testBuilderPattern() {
        UserUpdateDto builtDto = UserUpdateDto.builder()
                .fullname(VALID_FULLNAME)
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .role(VALID_ROLE)
                .active(VALID_ACTIVE)
                .build();

        assertThat(builtDto).isNotNull();
        assertEquals(builtDto.getFullname(), VALID_FULLNAME);
        assertEquals(builtDto.getUsername(), VALID_USERNAME);
        assertEquals(builtDto.getEmail(), VALID_EMAIL);
        assertEquals(builtDto.getPassword(), VALID_PASSWORD);
        assertEquals(builtDto.getRole(), VALID_ROLE);
        assertEquals(builtDto.getActive(), VALID_ACTIVE);
    }

    private void assertInvalidFieldValidation(String fieldName, Consumer<UserUpdateDto> setter) {
        setter.accept(userUpdateDto);
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(userUpdateDto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(fieldName));
    }

    private void assertValidFieldValidation(Consumer<UserUpdateDto> setter) {
        setter.accept(userUpdateDto);
        assertTrue(validator.validate(userUpdateDto).isEmpty());
    }

}
