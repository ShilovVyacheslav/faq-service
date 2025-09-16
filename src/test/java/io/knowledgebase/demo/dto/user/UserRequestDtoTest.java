package io.knowledgebase.demo.dto.user;

import io.knowledgebase.demo.dto.user.request.UserRequestDto;
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

public class UserRequestDtoTest extends UserDtoTestBase {

    private UserRequestDto userRequestDto;

    @BeforeEach
    void initUserRequestDto() {
        userRequestDto = new UserRequestDto(
                VALID_FULLNAME,
                VALID_USERNAME,
                VALID_EMAIL,
                VALID_PASSWORD,
                VALID_ROLE
        );
    }

    @Test
    @Order(1)
    @DisplayName("[1] Test valid UserRequestDto")
    void testValidUserRequestDto() {
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        assertTrue(violations.isEmpty(), "There should be no violations");
    }

    @ParameterizedTest
    @Order(2)
    @DisplayName("[2] Test invalid fullname validation")
    @NullSource
    @MethodSource("provideInvalidFullnames")
    void testInvalidFullnameValidation(String fullname) {
        assertInvalidFieldValidation("fullname", dto -> dto.setFullname(fullname));
    }

    @ParameterizedTest
    @Order(3)
    @DisplayName("[3] Test valid fullname validation")
    @MethodSource("provideValidFullnames")
    void testValidFullnameValidation(String fullname) {
        assertValidFieldValidation(dto -> dto.setFullname(fullname));
    }

    @ParameterizedTest
    @Order(4)
    @DisplayName("[4] Test invalid username validation")
    @NullSource
    @MethodSource("provideInvalidUsernames")
    void testInvalidUsernameValidation(String username) {
        assertInvalidFieldValidation("username", dto -> dto.setUsername(username));
    }

    @ParameterizedTest
    @Order(5)
    @DisplayName("[5] Test valid username validation")
    @MethodSource("provideValidUsernames")
    void testValidUsernameValidation(String username) {
        assertValidFieldValidation(dto -> dto.setUsername(username));
    }

    @ParameterizedTest
    @Order(6)
    @DisplayName("[6] Test invalid email validation")
    @NullSource
    @MethodSource("provideInvalidEmails")
    void testInvalidEmailValidation(String email) {
        assertInvalidFieldValidation("email", dto -> dto.setEmail(email));
    }

    @ParameterizedTest
    @Order(7)
    @DisplayName("[7] Test valid email validation")
    @MethodSource("provideValidEmails")
    void testValidEmailValidation(String email) {
        assertValidFieldValidation(dto -> dto.setEmail(email));
    }

    @ParameterizedTest
    @Order(8)
    @DisplayName("[8] Test invalid password validation")
    @NullSource
    @MethodSource("provideInvalidPasswords")
    void testInvalidPasswordValidation(String password) {
        assertInvalidFieldValidation("password", dto -> dto.setPassword(password));
    }

    @ParameterizedTest
    @Order(9)
    @DisplayName("[9] Test valid password validation")
    @MethodSource("provideValidPasswords")
    void testValidPasswordValidation(String password) {
        assertValidFieldValidation(dto -> dto.setPassword(password));
    }

    @ParameterizedTest
    @Order(10)
    @DisplayName("[10] Test invalid role validation")
    @NullSource
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
    @DisplayName("[12] Test builder pattern")
    void testBuilderPattern() {
        UserRequestDto builtDto = UserRequestDto.builder()
                .fullname(VALID_FULLNAME)
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .role(VALID_ROLE)
                .build();

        assertThat(builtDto).isNotNull();
        assertEquals(builtDto.getFullname(), VALID_FULLNAME);
        assertEquals(builtDto.getUsername(), VALID_USERNAME);
        assertEquals(builtDto.getEmail(), VALID_EMAIL);
        assertEquals(builtDto.getPassword(), VALID_PASSWORD);
        assertEquals(builtDto.getRole(), VALID_ROLE);
    }

    private void assertInvalidFieldValidation(String fieldName, Consumer<UserRequestDto> setter) {
        setter.accept(userRequestDto);
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(fieldName));
    }

    private void assertValidFieldValidation(Consumer<UserRequestDto> setter) {
        setter.accept(userRequestDto);
        assertTrue(validator.validate(userRequestDto).isEmpty());
    }

}
