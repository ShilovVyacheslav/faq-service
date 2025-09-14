package io.knowledgebase.demo.controller.admin;

import io.knowledgebase.demo.dto.user.request.UserFilterRequestDto;
import io.knowledgebase.demo.dto.user.request.UserRequestDto;
import io.knowledgebase.demo.dto.user.request.UserUpdateDto;
import io.knowledgebase.demo.dto.user.response.UserResponseDto;
import io.knowledgebase.demo.dto.user.response.UserUpdateResponseDto;
import io.knowledgebase.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequestDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(io.knowledgebase.demo.enums.Role).values())")
    public ResponseEntity<UserResponseDto> readUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.readUser(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserUpdateResponseDto> updateUser(@PathVariable Long id,
                                                            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        return ResponseEntity.ok(userService.updateUser(id, userUpdateDto));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> readAllUsers(@Valid UserFilterRequestDto userFilterRequestDto,
                                                              @PageableDefault(
                                                                      sort = "fullname",
                                                                      direction = Sort.Direction.ASC
                                                              ) Pageable pageable) {
        return ResponseEntity.ok(userService.readAllUsers(userFilterRequestDto, pageable));
    }

}
