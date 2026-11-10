package com.sneakerstore.controller;

import com.sneakerstore.dto.request.ChangePasswordRequest;
import com.sneakerstore.dto.request.LoginRequest;
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
}

