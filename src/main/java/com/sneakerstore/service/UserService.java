package com.sneakerstore.service;

import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.UserCreateRequest;
import com.sneakerstore.dto.request.UserUpdateRequest;
import com.sneakerstore.dto.response.RoleDto;
import com.sneakerstore.dto.response.UserDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    PageResponse<UserDto> searchUsers(String keyword, String status, Pageable pageable);

    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    List<RoleDto> getAllRoles();

    UserDto createUser(UserCreateRequest request);

    UserDto updateUser(Long id, UserUpdateRequest request);

    UserDto toggleStatus(Long id, String status);
}
