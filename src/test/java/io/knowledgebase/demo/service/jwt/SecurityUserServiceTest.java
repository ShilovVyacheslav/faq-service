package io.knowledgebase.demo.service.jwt;

import io.knowledgebase.demo.entity.User;
import io.knowledgebase.demo.enums.Role;
import io.knowledgebase.demo.exception.AuthException;
import io.knowledgebase.demo.repository.UserRepository;
import io.knowledgebase.demo.service.jwt.impl.SecurityUserService;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecurityUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityUserService securityUserService;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "test_user";

    @Test
    @Order(1)
    @DisplayName("[1] Load active user username -> returns UserDetails")
    void loadUserByUsername_ActiveUser_ReturnsSecurityUser() {

        User activeUser = createUser();

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(activeUser));

        assertThat(assertDoesNotThrow(() -> securityUserService.loadUserByUsername(TEST_USERNAME)))
                .satisfies(userDetails -> {
                    assertThat(userDetails).isNotNull();
                    assertThat(userDetails.getUsername()).isEqualTo(TEST_USERNAME);
                    assertTrue(userDetails.isEnabled());
                });

        verify(userRepository).findByUsername(TEST_USERNAME);

    }

    @Test
    @Order(2)
    @DisplayName("[2] Load non-existent user -> throws UserException")
    void loadUserByUsername_NonExistentUser_ThrowsUserException() {

        String username = "non_existent_user";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> securityUserService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("not found");

        verify(userRepository).findByUsername(username);

    }

    @Test
    @Order(3)
    @DisplayName("[3] Load inactive user -> throws AuthException")
    void loadUserByUsername_InactiveUser_ThrowsAuthException() {

        User inactiveUser = createUser();
        inactiveUser.setActive(false);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> securityUserService.loadUserByUsername(TEST_USERNAME))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Access denied");

        verify(userRepository).findByUsername(TEST_USERNAME);

    }

    @Test
    @Order(4)
    @DisplayName("[4] User roles are correctly mapped to authorities")
    void loadUserByUsername_UserRoles_MappedToAuthorities() {

        User user = createUser();
        user.setRole(Role.ADMIN);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        UserDetails userDetails = securityUserService.loadUserByUsername(TEST_USERNAME);

        assertThat(userDetails.getAuthorities())
                .asInstanceOf(InstanceOfAssertFactories.collection(GrantedAuthority.class))
                .extracting(authority -> authority.getAuthority().toLowerCase())
                .containsExactly(user.getRole().toString());

    }

    @ParameterizedTest
    @Order(5)
    @DisplayName("[5] Load user with invalid username -> throws UserException")
    @NullSource
    @ValueSource(strings = {"", "       "})
    void loadUserByUsername_InvalidUsername_ThrowsUserException(String username) {
        assertThatThrownBy(() -> securityUserService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("not found");
    }

    private User createUser() {
        return new User(
                TEST_USER_ID,
                null,
                TEST_USERNAME,
                "test.user@gmail.com",
                null,
                null,
                true,
                null,
                null
        );
    }

}
