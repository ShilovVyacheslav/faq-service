package io.knowledgebase.demo.service;

import io.knowledgebase.demo.common.validation.SortValidator;
import io.knowledgebase.demo.dto.user.UpdateResultDto;
import io.knowledgebase.demo.dto.user.request.UserFilterRequestDto;
import io.knowledgebase.demo.dto.user.request.UserRequestDto;
import io.knowledgebase.demo.dto.user.request.UserUpdateDto;
import io.knowledgebase.demo.dto.user.response.UserResponseDto;
import io.knowledgebase.demo.dto.user.response.UserUpdateResponseDto;
import io.knowledgebase.demo.entity.User;
import io.knowledgebase.demo.enums.Role;
import io.knowledgebase.demo.exception.UserException;
import io.knowledgebase.demo.mapper.UserMapper;
import io.knowledgebase.demo.repository.UserRepository;
import io.knowledgebase.demo.service.impl.UserServiceImpl;
import io.knowledgebase.demo.service.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static io.knowledgebase.demo.common.Constant.SUCCESS_USER_UPDATE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private SortValidator sortValidator;

    @InjectMocks
    private UserServiceImpl userService;

    private static final Long VALID_ID = 1L;
    private static final String VALID_FULLNAME = "Firstname Lastname";
    private static final String VALID_USERNAME = "username";
    private static final String VALID_EMAIL = "firstname.lastname@gmail.com";
    private static final String VALID_PASSWORD = "Password123@";
    private static final String ENCODED_PASSWORD = "$2a$10$UF0jw";
    private static final Role VALID_ROLE = Role.USER;
    private static final Boolean VALID_ACTIVE = true;
    private static final LocalDateTime VALID_DATE = LocalDateTime.now();

    private static final String NEW_FULLNAME = "New Fullname";
    private static final String NEW_USERNAME = "new_username";
    private static final String NEW_EMAIL = "new.user@gmail.com";
    private static final String NEW_PASSWORD = "NewPassword123!";
    private static final Role NEW_ROLE = Role.EXPERT;
    private static final Boolean NEW_ACTIVE = false;

    @Test
    @Order(1)
    @DisplayName("[1] Creation user with valid data -> returns UserResponseDto")
    void createUser_WithValidRequest_ReturnsUserResponseDto() {

        UserRequestDto userRequestDto = createValidUserRequestDto();

        User savedUser = new User(
                VALID_ID,
                userRequestDto.getFullname(),
                userRequestDto.getUsername(),
                userRequestDto.getEmail(),
                ENCODED_PASSWORD,
                userRequestDto.getRole(),
                VALID_ACTIVE,
                VALID_DATE,
                VALID_DATE
        );

        UserResponseDto expectedUserResponseDto = new UserResponseDto(
                savedUser.getId(),
                savedUser.getFullname(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.getActive()
        );

        when(userRepository.existsByUsernameIgnoreCase(userRequestDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userRequestDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userRequestDto.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userMapper.toEntity(userRequestDto, ENCODED_PASSWORD)).thenReturn(savedUser);
        when(userRepository.save(savedUser)).thenReturn(savedUser);
        when(userMapper.toResponseDto(savedUser)).thenReturn(expectedUserResponseDto);

        UserResponseDto actualUserResponseDto = userService.createUser(userRequestDto);

        assertThat(actualUserResponseDto)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedUserResponseDto);

        verify(userRepository).existsByUsernameIgnoreCase(userRequestDto.getUsername());
        verify(userRepository).existsByEmail(userRequestDto.getEmail());
        verify(passwordEncoder).encode(userRequestDto.getPassword());
        verify(userMapper).toEntity(userRequestDto, ENCODED_PASSWORD);
        verify(userRepository).save(savedUser);
        verify(userMapper).toResponseDto(savedUser);

        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);

    }

    @Test
    @Order(2)
    @DisplayName("[2] Creation user with not unique username -> throws UserException")
    void createUser_WithNotUniqueUsername_ThrowsUserException() {

        UserRequestDto userRequestDto = createValidUserRequestDto();

        when(userRepository.existsByUsernameIgnoreCase(userRequestDto.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequestDto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());

    }

    @Test
    @Order(3)
    @DisplayName("[3] Creation user with not unique email -> throws UserException")
    void createUser_WithNotUniqueEmail_ThrowsUserException() {

        UserRequestDto userRequestDto = createValidUserRequestDto();
        userRequestDto.setEmail(VALID_EMAIL.toUpperCase());

        when(userRepository.existsByEmail(userRequestDto.getEmail().toLowerCase())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequestDto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());

    }

    @Test
    @Order(4)
    @DisplayName("[4] Get existing user by ID -> returns UserResponseDto")
    void readUser_ExistingID_ReturnsUserResponseDto() {

        User user = createValidUser();

        UserResponseDto expectedUserResponseDto = new UserResponseDto(
                user.getId(),
                user.getFullname(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getActive()
        );

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(expectedUserResponseDto);

        UserResponseDto actualUserResponseDto = userService.readUser(VALID_ID);

        assertThat(actualUserResponseDto)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedUserResponseDto);

        verify(userRepository).findById(VALID_ID);
        verify(userMapper).toResponseDto(user);

        verifyNoMoreInteractions(userRepository, userMapper);

    }

    @Test
    @Order(5)
    @DisplayName("[5] Get non existing user by ID -> throws UserException")
    void readUser_NonExistingID_ThrowsUserException() {

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.readUser(VALID_ID))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("not found");

        verify(userRepository).findById(VALID_ID);

        verifyNoMoreInteractions(userRepository, userMapper);

    }

    @Test
    @Order(6)
    @DisplayName("[6] Update all fields -> success")
    void updateUser_AllFields_ReturnsUserUpdateResponseDto() {

        User existingUser = createValidUser();
        UserUpdateDto userUpdateDto = createValidUserUpdateDto();

        User updatedUser = new User(
                VALID_ID,
                NEW_FULLNAME,
                NEW_USERNAME,
                NEW_EMAIL,
                ENCODED_PASSWORD,
                NEW_ROLE,
                NEW_ACTIVE,
                VALID_DATE,
                VALID_DATE
        );

        UpdateResultDto updateResultDto = new UpdateResultDto(
                SUCCESS_USER_UPDATE.getResult(), SUCCESS_USER_UPDATE.getComment()
        );

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsernameIgnoreCase(userUpdateDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userUpdateDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userUpdateDto.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUpdateResponseDto(any(User.class), any(UpdateResultDto.class)))
                .thenReturn(new UserUpdateResponseDto(
                                VALID_ID,
                                NEW_FULLNAME,
                                NEW_USERNAME,
                                NEW_EMAIL,
                                NEW_ROLE,
                                VALID_DATE,
                                NEW_ACTIVE,
                                updateResultDto
                        )
                );

        UserUpdateResponseDto actualResponseDto = userService.updateUser(VALID_ID, userUpdateDto);

        assertThat(actualResponseDto)
                .hasFieldOrPropertyWithValue("fullname", NEW_FULLNAME)
                .hasFieldOrPropertyWithValue("username", NEW_USERNAME)
                .hasFieldOrPropertyWithValue("email", NEW_EMAIL)
                .hasFieldOrPropertyWithValue("role", NEW_ROLE)
                .hasFieldOrPropertyWithValue("active", NEW_ACTIVE);


        verify(userRepository).existsByUsernameIgnoreCase(userUpdateDto.getUsername());
        verify(userRepository).existsByEmail(userUpdateDto.getEmail());
        verify(passwordEncoder).encode(userUpdateDto.getPassword());
        verify(userMapper).updateFromDto(userUpdateDto, existingUser);
        verify(userRepository).save(any(User.class));

        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);

    }

    @Test
    @Order(7)
    @DisplayName("[7] Update only fullname -> partial update")
    void updateUser_PartialUpdate_ReturnsUserUpdateResponseDto() {

        User existingUser = createValidUser();
        UserUpdateDto userUpdateDto = new UserUpdateDto(
                NEW_FULLNAME,
                null,
                null,
                null,
                null,
                null
        );
        User updatedUser = createValidUser();
        updatedUser.setFullname(NEW_FULLNAME);
        UpdateResultDto updateResultDto = new UpdateResultDto(
                SUCCESS_USER_UPDATE.getResult(), SUCCESS_USER_UPDATE.getComment()
        );

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUpdateResponseDto(eq(updatedUser), any(UpdateResultDto.class)))
                .thenReturn(new UserUpdateResponseDto(
                        VALID_ID,
                        NEW_FULLNAME,
                        VALID_USERNAME,
                        VALID_EMAIL,
                        VALID_ROLE,
                        VALID_DATE,
                        VALID_ACTIVE,
                        updateResultDto
                ));

        UserUpdateResponseDto actualResponseDto = userService.updateUser(VALID_ID, userUpdateDto);

        assertThat(actualResponseDto)
                .hasFieldOrPropertyWithValue("fullname", NEW_FULLNAME)
                .hasFieldOrPropertyWithValue("username", existingUser.getUsername())
                .hasFieldOrPropertyWithValue("email", existingUser.getEmail())
                .hasFieldOrPropertyWithValue("role", existingUser.getRole())
                .hasFieldOrPropertyWithValue("active", existingUser.getActive());

        verify(userRepository).findById(VALID_ID);
        verify(userRepository).save(any(User.class));
        verify(userMapper).updateFromDto(userUpdateDto, existingUser);
        verify(userMapper).toUpdateResponseDto(eq(updatedUser), any(UpdateResultDto.class));
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);

    }

    @Test
    @Order(8)
    @DisplayName("[8] Update only role and active -> partial update")
    void updateUser_OnlyRoleAndActive_ReturnsUserUpdateResponseDto() {

        User existingUser = createValidUser();
        UserUpdateDto userUpdateDto = new UserUpdateDto(
                null,
                null,
                null,
                null,
                NEW_ROLE,
                NEW_ACTIVE
        );
        User updatedUser = createValidUser();
        updatedUser.setRole(NEW_ROLE);
        updatedUser.setActive(NEW_ACTIVE);
        UpdateResultDto updateResultDto = new UpdateResultDto(
                SUCCESS_USER_UPDATE.getResult(), SUCCESS_USER_UPDATE.getComment()
        );

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUpdateResponseDto(eq(updatedUser), any(UpdateResultDto.class)))
                .thenReturn(new UserUpdateResponseDto(
                        VALID_ID,
                        VALID_FULLNAME,
                        VALID_USERNAME,
                        VALID_EMAIL,
                        NEW_ROLE,
                        VALID_DATE,
                        NEW_ACTIVE,
                        updateResultDto
                ));

        UserUpdateResponseDto actualResponseDto = userService.updateUser(VALID_ID, userUpdateDto);

        assertThat(actualResponseDto)
                .hasFieldOrPropertyWithValue("fullname", existingUser.getFullname())
                .hasFieldOrPropertyWithValue("username", existingUser.getUsername())
                .hasFieldOrPropertyWithValue("email", existingUser.getEmail())
                .hasFieldOrPropertyWithValue("role", NEW_ROLE)
                .hasFieldOrPropertyWithValue("active", NEW_ACTIVE);

        verify(userRepository).findById(VALID_ID);
        verify(userRepository).save(any(User.class));
        verify(userMapper).updateFromDto(userUpdateDto, existingUser);
        verify(userMapper).toUpdateResponseDto(eq(updatedUser), any(UpdateResultDto.class));
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);

    }

    @Test
    @Order(9)
    @DisplayName("[9] Update with duplicate username -> throws UserException")
    void updateUser_DuplicateUsername_ThrowsUserException() {

        User existingUser = createValidUser();
        UserUpdateDto userUpdateDto = new UserUpdateDto(
                null,
                NEW_USERNAME,
                null,
                null,
                null,
                null
        );

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsernameIgnoreCase(userUpdateDto.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(VALID_ID, userUpdateDto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("already exists");

        verify(userRepository).findById(VALID_ID);
        verify(userRepository).existsByUsernameIgnoreCase(userUpdateDto.getUsername());
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);

    }

    @Test
    @Order(10)
    @DisplayName("[10] Update duplicate email with different case -> throws UserException")
    void updateUser_DuplicateEmail_ThrowsUserException() {

        User existingUser = createValidUser();
        UserUpdateDto userUpdateDto = new UserUpdateDto(
                null,
                null,
                NEW_EMAIL.toUpperCase(),
                null,
                null,
                null
        );

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(userUpdateDto.getEmail().toLowerCase())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(VALID_ID, userUpdateDto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("already exists");

        verify(userRepository).findById(VALID_ID);
        verify(userRepository).existsByEmail(userUpdateDto.getEmail().toLowerCase());
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);

    }

    @Test
    @Order(11)
    @DisplayName("[11] Update with null DTO -> throws Exception")
    void updateUser_NullDTO_ThrowsException() {

        User existingUser = createValidUser();

        when(userRepository.findById(null)).thenReturn(Optional.empty());
        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.updateUser(null, null))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("not found");

        assertThatThrownBy(() -> userService.updateUser(VALID_ID, null))
                .isInstanceOf(NullPointerException.class);

        verify(userRepository).findById(null);
        verify(userRepository).findById(VALID_ID);
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);

    }

    @Test
    @Order(12)
    @DisplayName("[12] Update not-existing user -> throws UserException")
    void updateUser_NotExistingUser_ThrowsUserException() {

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(VALID_ID, new UserUpdateDto()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("not found");

        verify(userRepository).findById(VALID_ID);
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);

    }

    @Test
    @Order(13)
    @DisplayName("[13] Read all with filters -> returns filtered results")
    void readAllUsers_WithFilters_ReturnsFiltered() {

        UserFilterRequestDto userFilterRequestDto = new UserFilterRequestDto(
                null,
                "user",
                null,
                null,
                true,
                null,
                null,
                null,
                null
        );

        User user1 = createValidUser();
        User user2 = createValidUser();
        user2.setUsername(NEW_FULLNAME);
        user2.setEmail(NEW_EMAIL);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<User>(List.of(user1, user2));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toResponseDto(user1)).thenReturn(new UserResponseDto(
                VALID_ID, VALID_FULLNAME, VALID_USERNAME, VALID_EMAIL, VALID_ROLE, VALID_DATE, VALID_DATE, VALID_ACTIVE
        ));
        when(userMapper.toResponseDto(user2)).thenReturn(new UserResponseDto(
                VALID_ID, NEW_FULLNAME, NEW_USERNAME, NEW_EMAIL, NEW_ROLE, VALID_DATE, VALID_DATE, VALID_ACTIVE
        ));

        Page<UserResponseDto> result = userService.readAllUsers(userFilterRequestDto, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent())
                .asInstanceOf(list(UserResponseDto.class))
                .hasSize(2)
                .extracting(UserResponseDto::getUsername)
                .containsExactly(VALID_USERNAME, NEW_USERNAME);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo(VALID_USERNAME);
        assertThat(result.getContent().get(1).getUsername()).isEqualTo(NEW_USERNAME);

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(userMapper).toResponseDto(user1);
        verify(userMapper).toResponseDto(user2);
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);

    }

    @Test
    @Order(14)
    @DisplayName("[14] Email normalization on create -> converts to lowercase")
    void createUser_EmailNormalization_ConvertsToLowerCase() {

        UserRequestDto userRequestDto = createValidUserRequestDto();
        userRequestDto.setEmail(userRequestDto.getEmail().toUpperCase());

        User savedUser = createValidUser();
        savedUser.setEmail(savedUser.getEmail().toLowerCase());

        when(userRepository.existsByUsernameIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
        when(userMapper.toEntity(any(), any())).thenReturn(savedUser);
        when(userRepository.save(any())).thenReturn(savedUser);
        when(userMapper.toResponseDto(savedUser)).thenReturn(
                new UserResponseDto(
                        savedUser.getId(),
                        savedUser.getFullname(),
                        savedUser.getUsername(),
                        savedUser.getEmail(),
                        savedUser.getRole(),
                        savedUser.getCreatedAt(),
                        savedUser.getUpdatedAt(),
                        savedUser.getActive()
                )
        );

        UserResponseDto userResponseDto = userService.createUser(userRequestDto);

        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getEmail()).isEqualTo(userRequestDto.getEmail().toLowerCase());

        verify(userRepository).existsByUsernameIgnoreCase(userRequestDto.getUsername());
        verify(userRepository).existsByEmail(userRequestDto.getEmail().toLowerCase());
        verify(passwordEncoder).encode(userRequestDto.getPassword());
        verify(userMapper).toEntity(userRequestDto, ENCODED_PASSWORD);
        verify(userRepository).save(savedUser);
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);

    }

    @Test
    @Order(15)
    @DisplayName("[15] Get user by valid jwt -> returns user")
    void getUser_ValidJwt_ReturnsUser() {

        User user = createValidUser();

        when(jwtService.getUserName()).thenReturn(VALID_USERNAME);
        when(userRepository.findByUsername(VALID_USERNAME)).thenReturn(Optional.of(user));

        User result = userService.getUserByJwt();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(user);

        verify(jwtService).getUserName();
        verify(userRepository).findByUsername(VALID_USERNAME);
        verifyNoMoreInteractions(jwtService, userRepository);

    }

    @Test
    @Order(16)
    @DisplayName("[16] Get user by invalid jwt -> throws exception")
    void getUser_InvalidJwt_ThrowsException() {

        String invalidUsername = "invalid";

        when(jwtService.getUserName()).thenReturn(invalidUsername);
        when(userRepository.findByUsername(invalidUsername)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByJwt())
                .isInstanceOf(UserException.class)
                .hasMessageContaining("not found");

        verify(jwtService).getUserName();
        verify(userRepository).findByUsername(invalidUsername);
        verifyNoMoreInteractions(jwtService, userRepository);

    }

    private UserRequestDto createValidUserRequestDto() {
        return new UserRequestDto(
                VALID_FULLNAME,
                VALID_USERNAME,
                VALID_EMAIL,
                VALID_PASSWORD,
                VALID_ROLE
        );
    }

    private UserUpdateDto createValidUserUpdateDto() {
        return new UserUpdateDto(
                NEW_FULLNAME,
                NEW_USERNAME,
                NEW_EMAIL,
                NEW_PASSWORD,
                NEW_ROLE,
                NEW_ACTIVE
        );
    }

    private User createValidUser() {
        return new User(
                VALID_ID,
                VALID_FULLNAME,
                VALID_USERNAME,
                VALID_EMAIL,
                ENCODED_PASSWORD,
                VALID_ROLE,
                VALID_ACTIVE,
                VALID_DATE,
                VALID_DATE
        );
    }

}
