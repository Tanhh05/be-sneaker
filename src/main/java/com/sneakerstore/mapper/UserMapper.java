package com.sneakerstore.mapper;

import com.sneakerstore.dto.response.RoleDto;
import com.sneakerstore.dto.response.UserDto;
import com.sneakerstore.entity.Role;
import com.sneakerstore.entity.User;

public interface UserMapper {
    UserDto toDto(User user);
    RoleDto toRoleDto(Role role);
}
