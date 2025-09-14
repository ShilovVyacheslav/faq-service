package io.knowledgebase.demo.mapper;

import io.knowledgebase.demo.dto.user.UpdateResultDto;
import io.knowledgebase.demo.dto.user.request.UserRequestDto;
import io.knowledgebase.demo.dto.user.request.UserUpdateDto;
import io.knowledgebase.demo.dto.user.response.UserResponseDto;
import io.knowledgebase.demo.dto.user.response.UserUpdateResponseDto;
import io.knowledgebase.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    UserResponseDto toResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "password", source = "encodedPassword")
    User toEntity(UserRequestDto userRequestDto, String encodedPassword);

    void updateFromDto(UserUpdateDto userUpdateDto, @MappingTarget User user);

    @Mapping(target = "response", source = "updateResultDto")
    UserUpdateResponseDto toUpdateResponseDto(User user, UpdateResultDto updateResultDto);
}
