package com.sneakerstore.service.impl;

import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.common.util.SecurityUtils;
import com.sneakerstore.dto.request.UserCreateRequest;
import com.sneakerstore.dto.request.UserUpdateRequest;
import com.sneakerstore.dto.response.RoleDto;
import com.sneakerstore.dto.response.UserDto;
import com.sneakerstore.entity.Role;
import com.sneakerstore.entity.User;
import com.sneakerstore.mapper.UserMapper;
import com.sneakerstore.repository.RoleRepository;
import com.sneakerstore.repository.UserRepository;
import com.sneakerstore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> searchUsers(String keyword, String status, Pageable pageable) {
        // Bước 3 & 5: Thực thi truy vấn phân trang và map sang Response DTO
        Page<UserDto> page = userRepository.searchUsers(keyword, status, pageable)
                .map(userMapper::toDto);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAllByOrderByIdAsc().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại bản ghi)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        // Bước 5: Output Mapping
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(userMapper::toRoleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra trùng username, email và tính hợp lệ của danh sách Role)
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại: " + request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail().trim())) {
            throw new BusinessException("Email đã được sử dụng: " + request.getEmail());
        }
        Set<Role> roles = resolveRoles(request.getRoles());

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Khởi tạo tài khoản và mã hóa mật khẩu)
        String currentActor = SecurityUtils.getCurrentUsername();
        User user = User.builder()
                .username(request.getUsername().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .email(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null)
                .phone(request.getPhone())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .roles(roles)
                // Bước 4: Ghi vết / Audit
                .createdBy(currentActor)
                .updatedBy(currentActor)
                .build();

        // Bước 5: Output Mapping
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại tài khoản và trùng lặp email)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().trim().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(request.getEmail().trim())) {
            throw new BusinessException("Email đã được sử dụng: " + request.getEmail());
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Cập nhật thông tin và phân quyền)
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null);
        user.setPhone(request.getPhone());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(request.getStatus());
        }
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }
        user.setRoles(resolveRoles(request.getRoles()));

        // Bước 4: Ghi vết / Audit
        user.setUpdatedBy(SecurityUtils.getCurrentUsername());

        // Bước 5: Output Mapping
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto toggleStatus(Long id, String status) {
        // Bước 1: Validation nghiệp vụ (Bảo vệ tài khoản quản trị mặc định admin)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        if ("admin".equalsIgnoreCase(user.getUsername()) && !"ACTIVE".equals(status)) {
            throw new BusinessException("Không thể khóa tài khoản quản trị mặc định (admin)");
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Cập nhật trạng thái)
        user.setStatus(status);

        // Bước 4: Ghi vết / Audit
        user.setUpdatedBy(SecurityUtils.getCurrentUsername());

        // Bước 5: Output Mapping
        return userMapper.toDto(userRepository.save(user));
    }

    private Set<Role> resolveRoles(List<String> roleCodes) {
        Set<Role> roles = new HashSet<>();
        for (String code : roleCodes) {
            Role role = roleRepository.findByRoleCode(code.toUpperCase())
                    .orElseThrow(() -> new BusinessException("Vai trò không hợp lệ: " + code));
            roles.add(role);
        }
        return roles;
    }
}

