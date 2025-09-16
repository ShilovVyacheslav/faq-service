package io.knowledgebase.demo.mapper;

import io.knowledgebase.demo.dto.user.UpdateResultDto;
import io.knowledgebase.demo.dto.user.request.UserRequestDto;
import io.knowledgebase.demo.dto.user.request.UserUpdateDto;
import io.knowledgebase.demo.dto.user.response.UserResponseDto;
import io.knowledgebase.demo.dto.user.response.UserUpdateResponseDto;
import io.knowledgebase.demo.entity.User;
import io.knowledgebase.demo.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.knowledgebase.demo.common.Constant.SUCCESS_USER_UPDATE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserMapperTest {

    private final UserMapperImpl userMapper = new UserMapperImpl();

    private static final Long VALID_ID = 1L;
    private static final String VALID_FULLNAME = "Firstname Lastname";
    private static final String VALID_USERNAME = "username";
    private static final String VALID_EMAIL = "firstname.lastname@gmail.com";
    private static final String VALID_PASSWORD = "Password123@";
    private static final String ENCODED_PASSWORD = "$2a$10$UF0jw";
    private static final Role VALID_ROLE = Role.USER;
    private static final Boolean VALID_ACTIVE = true;
    private static final LocalDateTime VALID_DATE = LocalDateTime.now();

    @Test
    @DisplayName("Mapping UserRequestDto to User")
    void mapUserRequestDtoToUser_ShouldMapFieldsAndSetDefaults() {

        UserRequestDto userRequestDto = createValidUserRequestDto();
        userRequestDto.setEmail(userRequestDto.getEmail().toUpperCase());

        User user = userMapper.toEntity(userRequestDto, ENCODED_PASSWORD);

        assertThat(user).isNotNull().satisfies(entity -> {
            assertThat(entity.getId()).isNull();
            assertEquals(entity.getFullname(), userRequestDto.getFullname());
            assertEquals(entity.getUsername(), userRequestDto.getUsername());
            assertEquals(entity.getEmail(), userRequestDto.getEmail().toUpperCase());
            assertEquals(entity.getPassword(), ENCODED_PASSWORD);
            assertEquals(entity.getRole(), userRequestDto.getRole());
            assertTrue(entity.getActive());
            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
        });
    }

    @Test
    @DisplayName("Mapping UserRequestDto as null to User")
    void mapUserRequestDtoToUser_ShouldHandleNullInput() {
        assertThat(userMapper.toEntity(null, null)).isNull();

        User user = userMapper.toEntity(null, ENCODED_PASSWORD);

        assertThat(user).isNotNull().hasAllNullFieldsOrPropertiesExcept("password", "active");
        assertEquals(user.getPassword(), ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("Mapping User to UserResponseDto")
    void mapUserToUserResponseDto_ShouldMapAllFieldsCorrectly() {

        User user = createValidUser();

        UserResponseDto userResponseDto = userMapper.toResponseDto(user);

        assertThat(userResponseDto).isNotNull().satisfies(dto -> {
            assertEquals(dto.getId(), user.getId());
            assertEquals(dto.getFullname(), user.getFullname());
            assertEquals(dto.getUsername(), user.getUsername());
            assertEquals(dto.getEmail(), user.getEmail());
            assertEquals(dto.getRole(), user.getRole());
            assertEquals(dto.getActive(), user.getActive());
            assertEquals(dto.getCreatedAt(), user.getCreatedAt());
            assertEquals(dto.getUpdatedAt(), user.getUpdatedAt());
        });
    }

    @Test
    @DisplayName("Mapping User as null to UserResponseDto")
    void mapUserToUserResponseDto_ShouldHandleNullInput() {
        assertThat(userMapper.toResponseDto(null)).isNull();
    }

    @Test
    @DisplayName("Mapping UserUpdateDto to User")
    void mapUserUpdateDtoToUser_ShouldUpdateOnlyNonNullFields() {

        UserUpdateDto userUpdateDto = new UserUpdateDto(
                "New Fullname",
                null,
                null,
                "@Password321",
                null,
                null
        );

        User expectedUser = createValidUser();
        User actualUser = createValidUser();

        userMapper.updateFromDto(userUpdateDto, actualUser);

        assertEquals(actualUser.getFullname(), userUpdateDto.getFullname());
        assertEquals(actualUser.getPassword(), userUpdateDto.getPassword());

        assertEquals(actualUser.getUsername(), expectedUser.getUsername());
        assertEquals(actualUser.getEmail(), expectedUser.getEmail());
        assertEquals(actualUser.getRole(), expectedUser.getRole());
        assertEquals(actualUser.getActive(), expectedUser.getActive());
        assertEquals(actualUser.getCreatedAt(), expectedUser.getCreatedAt());
        assertEquals(actualUser.getUpdatedAt(), expectedUser.getUpdatedAt());
    }

    @Test
    @DisplayName("Mapping UserUpdateDto as null to User")
    void mapUserUpdateDtoToUser_ShouldHandleNullInput() {
        User expectedUser = createValidUser();
        User actualUser = createValidUser();
        userMapper.updateFromDto(null, actualUser);
        assertThat(actualUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("Mapping User to UserUpdateResponseDto")
    void mapUserToUserUpdateResponseDto_ShouldMapAllFieldsCorrectly() {
        User user = createValidUser();

        UpdateResultDto updateResultDto = new UpdateResultDto(
                SUCCESS_USER_UPDATE.getResult(), SUCCESS_USER_UPDATE.getComment()
        );
        UserUpdateResponseDto userUpdateResponseDto = userMapper.toUpdateResponseDto(user, updateResultDto);

        assertThat(userUpdateResponseDto).isNotNull().satisfies(dto -> {
            assertEquals(dto.getId(), user.getId());
            assertEquals(dto.getFullname(), user.getFullname());
            assertEquals(dto.getUsername(), user.getUsername());
            assertEquals(dto.getEmail(), user.getEmail());
            assertEquals(dto.getRole(), user.getRole());
            assertEquals(dto.getActive(), user.getActive());
            assertEquals(dto.getUpdatedAt(), user.getUpdatedAt());
            assertThat(dto.getResponse()).usingRecursiveComparison().isEqualTo(updateResultDto);
        });
    }

    @Test
    @DisplayName("Mapping User as null to UserUpdateResponseDto")
    void mapUserToUserUpdateResponseDto_ShouldHandleNullInput() {
        assertThat(userMapper.toUpdateResponseDto(null, null)).isNull();

        UpdateResultDto updateResultDto = new UpdateResultDto(
                SUCCESS_USER_UPDATE.getResult(), SUCCESS_USER_UPDATE.getComment()
        );

        UserUpdateResponseDto userUpdateResponseDto = userMapper.toUpdateResponseDto(null, updateResultDto);

        assertThat(userUpdateResponseDto).isNotNull()
                .hasAllNullFieldsOrPropertiesExcept("response");
        assertThat(userUpdateResponseDto.getResponse()).usingRecursiveComparison().isEqualTo(updateResultDto);
    }

    private User createValidUser() {
        return new User(
                VALID_ID,
                VALID_FULLNAME,
                VALID_USERNAME,
                VALID_EMAIL,
                VALID_PASSWORD,
                VALID_ROLE,
                VALID_ACTIVE,
                VALID_DATE,
                VALID_DATE
        );
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
                VALID_FULLNAME,
                VALID_USERNAME,
                VALID_EMAIL,
                VALID_PASSWORD,
                VALID_ROLE,
                VALID_ACTIVE
        );
    }

    private UserResponseDto createValidUserResponseDto() {
        return new UserResponseDto(
                VALID_ID,
                VALID_FULLNAME,
                VALID_USERNAME,
                VALID_EMAIL,
                VALID_ROLE,
                VALID_DATE,
                VALID_DATE,
                VALID_ACTIVE
        );
    }

    private UserUpdateResponseDto createValidUserUpdateResponseDto() {
        return new UserUpdateResponseDto(
                VALID_ID,
                VALID_FULLNAME,
                VALID_USERNAME,
                VALID_EMAIL,
                VALID_ROLE,
                VALID_DATE,
                VALID_ACTIVE,
                new UpdateResultDto(
                        SUCCESS_USER_UPDATE.getResult(),
                        SUCCESS_USER_UPDATE.getComment()
                )
        );
    }

}
