package com.sneakerstore.mapper.impl;

import com.sneakerstore.mapper.UserMapper;

import com.sneakerstore.dto.response.RoleDto;
import com.sneakerstore.dto.response.UserDto;
import com.sneakerstore.entity.Role;
import com.sneakerstore.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles() != null
                        ? user.getRoles().stream()
                                .map(Role::getRoleCode)
                                .sorted()
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }

    @Override
    public RoleDto toRoleDto(Role role) {
        if (role == null) {
            return null;
        }
        return RoleDto.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .status(role.getStatus())
                .build();
    }
}

