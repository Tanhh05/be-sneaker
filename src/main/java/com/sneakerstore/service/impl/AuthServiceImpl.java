package com.sneakerstore.service.impl;

import com.sneakerstore.dto.request.ChangePasswordRequest;
import com.sneakerstore.dto.request.LoginRequest;
import com.sneakerstore.dto.response.LoginResponse;
import com.sneakerstore.service.AuthService;
import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.util.SecurityUtils;
import com.sneakerstore.security.jwt.JwtTokenProvider;
import com.sneakerstore.dto.response.UserDto;
import com.sneakerstore.entity.User;
import com.sneakerstore.mapper.UserMapper;
import com.sneakerstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại tài khoản và trạng thái hoạt động)
        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new LockedException("Tài khoản đã bị khóa hoặc ngừng hoạt động");
        }

        // Bước 2: Quản lý Transaction (@Transactional đảm bảo cập nhật lastLoginAt đồng bộ)
        try {
            // Bước 3: Thực thi logic lõi (Xác thực thông tin đăng nhập & cấp phát JWT token)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword())
            );
            String token = jwtTokenProvider.generateToken(authentication);

            // Bước 4: Ghi vết / Audit (Cập nhật thời điểm đăng nhập gần nhất)
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Bước 5: Output Mapping (Đóng gói Entity sang LoginResponse DTO)
            return LoginResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .user(userMapper.toDto(user))
                    .build();
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu");
        }
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // Bước 1: Validation nghiệp vụ (Xác thực người dùng hiện tại và kiểm tra mật khẩu cũ)
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản hiện tại"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không chính xác");
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Mã hóa mật khẩu mới)
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        // Bước 4: Ghi vết / Audit (Ghi nhận người thực hiện cập nhật)
        user.setUpdatedBy(username);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tài khoản từ SecurityContext)
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản hiện tại"));

        // Bước 5: Output Mapping (Chuyển đổi sang UserDto)
        return userMapper.toDto(user);
    }
}

