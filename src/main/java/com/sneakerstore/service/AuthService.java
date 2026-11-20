package com.sneakerstore.service;

import com.sneakerstore.dto.request.ChangePasswordRequest;
import com.sneakerstore.dto.request.GoogleLoginRequest;
import com.sneakerstore.dto.request.LoginRequest;
import com.sneakerstore.dto.request.RegisterRequest;
import com.sneakerstore.dto.request.UpdateProfileRequest;
import com.sneakerstore.dto.response.LoginResponse;
import com.sneakerstore.dto.response.UserDto;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    LoginResponse loginWithGoogle(GoogleLoginRequest request);

    void changePassword(ChangePasswordRequest request);

    UserDto getCurrentUser();

    UserDto updateProfile(UpdateProfileRequest request);
}
