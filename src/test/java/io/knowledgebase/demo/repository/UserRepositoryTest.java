package io.knowledgebase.demo.repository;

import io.knowledgebase.demo.entity.User;
import io.knowledgebase.demo.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;

    private static final String VALID_FULLNAME = "Test User";
    private static final String VALID_USERNAME = "test_user";
    private static final String VALID_EMAIL = "test.user@gmail.com";
    private static final String ENCODED_PASSWORD = "$2a$10$UF0jw";
    private static final Role VALID_ROLE = Role.USER;

    @BeforeEach
    void init() {
        testUser = createValidUser();
        testUser.setFullname(VALID_FULLNAME);
        testUser.setUsername(VALID_USERNAME);
        testUser.setEmail(VALID_EMAIL);

        userRepository.saveAndFlush(testUser);
        entityManager.clear();
    }

    @Test
    @Order(0)
    @DisplayName("[0] Save user with correct input")
    void saveUser_ShouldPersistNewUser() {

        User newUser = createValidUser();

        User savedUser = userRepository.saveAndFlush(newUser);
        entityManager.clear();

        Optional<User> optionalUser = userRepository.findById(savedUser.getId());
        assertTrue(optionalUser.isPresent());

        User foundUser = optionalUser.get();
        assertThat(foundUser).usingRecursiveComparison().isEqualTo(newUser);
    }

    @ParameterizedTest
    @Order(1)
    @DisplayName("[1] Find by non-existent username")
    @NullSource
    @ValueSource(strings = {"123", "admin", "user777"})
    void findByUsername_ShouldReturnEmpty(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        assertThat(user).isEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("[2] Find by existent username")
    void findByUsername_ShouldReturnUser() {
        Optional<User> user = userRepository.findByUsername(VALID_USERNAME);
        assertThat(user).isPresent().get().usingRecursiveComparison().isEqualTo(testUser);
    }

    @ParameterizedTest
    @Order(3)
    @DisplayName("[3] Check existence by non-existent username")
    @NullSource
    @ValueSource(strings = {"123", "admin", "user777"})
    void checkExistenceByUsername_ShouldReturnFalse(String username) {
        assertFalse(userRepository.existsByUsernameIgnoreCase(username));
    }

    @Test
    @Order(4)
    @DisplayName("[4] Check existence by existent username")
    void checkExistenceByUsername_ShouldReturnTrue() {
        assertTrue(userRepository.existsByUsernameIgnoreCase(VALID_USERNAME.toUpperCase()));
    }

    @ParameterizedTest
    @Order(5)
    @DisplayName("[5] Check existence by non-existent email")
    @NullSource
    @ValueSource(strings = {"123", "admin", "user777"})
    void checkExistenceByEmail_ShouldReturnFalse(String email) {
        assertFalse(userRepository.existsByEmail(email));
    }

    @Test
    @Order(6)
    @DisplayName("[6] Check existence by existent email")
    void checkExistenceByEmail_ShouldReturnTrue() {
        assertTrue(userRepository.existsByEmail(VALID_EMAIL));
    }

    @Test
    @Order(7)
    @DisplayName("[7] Save user without required fullname, must call exception")
    void saveUserWithoutFullname_ShouldThrowException() {

        User user = createValidUser();
        user.setFullname(null);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Order(8)
    @DisplayName("[8] Save user without required username, must call exception")
    void saveUserWithoutUsername_ShouldThrowException() {

        User user = createValidUser();
        user.setUsername(null);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Order(9)
    @DisplayName("[9] Save user without required email, must call exception")
    void saveUserWithoutEmail_ShouldThrowException() {
        User user = createValidUser();
        user.setEmail(null);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Order(10)
    @DisplayName("[10] Save user without required role, must call exception")
    void saveUserWithoutRole_ShouldThrowException() {

        User user = createValidUser();
        user.setRole(null);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Order(11)
    @DisplayName("[11] Save user without required active, must call exception")
    void saveUserWithoutActive_ShouldThrowException() {

        User user = createValidUser();
        user.setActive(null);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("NULL not allowed for column");
    }

    @Test
    @Order(12)
    @DisplayName("[12] Save user with not unique username, should throw exception")
    void saveUserWithNotUniqueUsername_ShouldThrowException() {

        User user = createValidUser();
        user.setUsername(VALID_USERNAME);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasRootCauseInstanceOf(JdbcSQLIntegrityConstraintViolationException.class);
    }

    @Test
    @Order(13)
    @DisplayName("[13] Save user with not unique email, should throw exception")
    void saveUserWithNotUniqueEmail_ShouldThrowException() {

        User user = createValidUser();
        user.setEmail(VALID_EMAIL);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasRootCauseInstanceOf(JdbcSQLIntegrityConstraintViolationException.class);
    }

    @Test
    @Order(14)
    @DisplayName("[14] Save user must automatically set createdAt and updatedAt")
    void saveUser_ShouldSetCreatedAtAndUpdatedAt() {
        User newUser = createValidUser();
        User savedUser = userRepository.saveAndFlush(newUser);

        assertThat(savedUser.getCreatedAt()).isNotNull().isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(savedUser.getUpdatedAt()).isNotNull().isEqualTo(savedUser.getCreatedAt());
    }

    @Test
    @Order(15)
    @DisplayName("[15] Update user, should update the field updatedAt")
    void updateUser_ShouldUpdateFieldUpdatedAt() {

        User user = userRepository.findById(testUser.getId()).orElseThrow();
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();

        user.setFullname("Updated User");
        userRepository.saveAndFlush(user);
        entityManager.clear();

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();

        assertThat(updatedUser.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @Order(16)
    @DisplayName("[16] Email should be normalized when save")
    void saveUser_ShouldNormalizeEmail() {

        String email = "MixedCase@Gmail.COM";
        User newUser = createValidUser();
        newUser.setEmail(email);

        User savedUser = userRepository.saveAndFlush(newUser);

        assertEquals(savedUser.getEmail(), email.toLowerCase());
    }

    private User createValidUser() {
        return new User(
                null,
                "New User",
                "new_user",
                "new.user@gmail.com",
                ENCODED_PASSWORD,
                VALID_ROLE,
                true,
                null,
                null
        );
    }

}
