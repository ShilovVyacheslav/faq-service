package io.knowledgebase.demo.service;

import io.knowledgebase.demo.dto.user.request.UserFilterRequestDto;
import io.knowledgebase.demo.dto.user.request.UserRequestDto;
import io.knowledgebase.demo.dto.user.request.UserUpdateDto;
import io.knowledgebase.demo.dto.user.response.UserResponseDto;
import io.knowledgebase.demo.dto.user.response.UserUpdateResponseDto;
import io.knowledgebase.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponseDto createUser(UserRequestDto userRequestDto);

    UserResponseDto readUser(Long id);

    UserUpdateResponseDto updateUser(Long id, UserUpdateDto userUpdateDto);

    Page<UserResponseDto> readAllUsers(UserFilterRequestDto userFilterRequestDto, Pageable pageable);

    User getUserByJwt();

}
