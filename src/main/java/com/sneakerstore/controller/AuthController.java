package com.sneakerstore.controller;

import com.sneakerstore.dto.request.ChangePasswordRequest;
import com.sneakerstore.dto.request.GoogleLoginRequest;
import com.sneakerstore.dto.request.LoginRequest;
import com.sneakerstore.dto.request.RegisterRequest;
import com.sneakerstore.dto.request.UpdateProfileRequest;
import com.sneakerstore.dto.response.LoginResponse;
import com.sneakerstore.service.AuthService;
import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.dto.response.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Đăng nhập thành công", authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Đăng ký tài khoản thành công", authService.register(request));
    }

    @PostMapping("/google")
    public ApiResponse<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ApiResponse.ok("Đăng nhập Google thành công", authService.loginWithGoogle(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok("Đăng xuất thành công", null);
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.ok("Đổi mật khẩu thành công", null);
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me() {
        return ApiResponse.ok(authService.getCurrentUser());
    }

    @PutMapping("/profile")
    public ApiResponse<UserDto> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok("Cập nhật thông tin tài khoản thành công", authService.updateProfile(request));
    }
}

