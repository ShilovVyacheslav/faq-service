package io.knowledgebase.demo.service.impl;

import io.knowledgebase.demo.common.validation.SortValidator;
import io.knowledgebase.demo.dto.user.UpdateResultDto;
import io.knowledgebase.demo.dto.user.request.UserFilterRequestDto;
import io.knowledgebase.demo.dto.user.request.UserRequestDto;
import io.knowledgebase.demo.dto.user.request.UserUpdateDto;
import io.knowledgebase.demo.dto.user.response.UserResponseDto;
import io.knowledgebase.demo.dto.user.response.UserUpdateResponseDto;
import io.knowledgebase.demo.entity.User;
import io.knowledgebase.demo.exception.UserException;
import io.knowledgebase.demo.mapper.UserMapper;
import io.knowledgebase.demo.repository.UserRepository;
import io.knowledgebase.demo.service.UserService;
import io.knowledgebase.demo.service.jwt.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

import static io.knowledgebase.demo.common.Constant.SUCCESS_USER_UPDATE;
import static io.knowledgebase.demo.repository.UserRepository.UserSpec.createdAtBetween;
import static io.knowledgebase.demo.repository.UserRepository.UserSpec.emailContains;
import static io.knowledgebase.demo.repository.UserRepository.UserSpec.fullnameContains;
import static io.knowledgebase.demo.repository.UserRepository.UserSpec.isActive;
import static io.knowledgebase.demo.repository.UserRepository.UserSpec.roleEquals;
import static io.knowledgebase.demo.repository.UserRepository.UserSpec.updatedAtBetween;
import static io.knowledgebase.demo.repository.UserRepository.UserSpec.usernameContains;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SortValidator sortValidator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "fullname", "username", "email", "role", "active", "createdAt", "updatedAt"
    );

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {

        log.info("Creating new user with username: {}", userRequestDto.getUsername());

        validateByUsername(userRequestDto.getUsername());
        validateByEmail(userRequestDto.getEmail());

        User user = userMapper.toEntity(userRequestDto, passwordEncoder.encode(userRequestDto.getPassword()));

        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());

        return userMapper.toResponseDto(savedUser);
    }

    @Override
    public UserResponseDto readUser(Long id) {
        return userMapper.toResponseDto(userRepository.findById(id).orElseThrow(() -> UserException.userNotFound(id)));
    }

    @Override
    @Transactional
    public UserUpdateResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) {

        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id).orElseThrow(() -> UserException.userNotFound(id));

        validateUsernameUpdate(user, userUpdateDto);
        validateEmailUpdate(user, userUpdateDto);

        updateUserFields(user, userUpdateDto);

        User updatedUser = userRepository.save(user);

        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return buildSuccessResponse(updatedUser);
    }

    @Override
    public Page<UserResponseDto> readAllUsers(UserFilterRequestDto filter, Pageable pageable) {
        sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);
        return userRepository.findAll(getSpecification(filter), pageable).map(userMapper::toResponseDto);
    }

    @Override
    public User getUserByJwt() {
        String username = jwtService.getUserName();
        return userRepository.findByUsername(username).orElseThrow(() -> UserException.userNotFound(username));
    }

    private Specification<User> getSpecification(UserFilterRequestDto filter) {
        return filter == null ? Specification.allOf() : Specification.allOf(
                fullnameContains(filter.getFullname()),
                usernameContains(filter.getUsername()),
                emailContains(filter.getEmail()),
                roleEquals(filter.getRole()),
                isActive(filter.getActive()),
                createdAtBetween(filter.getCreatedAtFrom(), filter.getCreatedAtTo()),
                updatedAtBetween(filter.getUpdatedAtFrom(), filter.getUpdatedAtTo())
        );
    }

    private void validateByUsername(String username) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw UserException.userAlreadyExists(username);
        }
    }

    private void validateByEmail(String email) {
        if (email == null) {
            return;
        }
        if (userRepository.existsByEmail(email.toLowerCase())) {
            throw UserException.userAlreadyExists(email);
        }
    }

    private void validateUsernameUpdate(User user, UserUpdateDto userUpdateDto) {
        if (userUpdateDto.getUsername() != null && !userUpdateDto.getUsername().equalsIgnoreCase(user.getUsername())) {
            validateByUsername(userUpdateDto.getUsername());
        }
    }

    private void validateEmailUpdate(User user, UserUpdateDto userUpdateDto) {
        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().equalsIgnoreCase(user.getEmail())) {
            validateByEmail(userUpdateDto.getEmail());
        }
    }

    private void updateUserFields(User user, UserUpdateDto userUpdateDto) {
        userMapper.updateFromDto(userUpdateDto, user);
        if (userUpdateDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }
    }

    private UserUpdateResponseDto buildSuccessResponse(User user) {
        UpdateResultDto updateResultDto = UpdateResultDto.builder()
                .result(SUCCESS_USER_UPDATE.getResult())
                .comment(SUCCESS_USER_UPDATE.getComment())
                .build();
        return userMapper.toUpdateResponseDto(user, updateResultDto);
    }
}
