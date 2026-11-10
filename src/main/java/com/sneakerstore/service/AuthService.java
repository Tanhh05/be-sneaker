package com.sneakerstore.service;

import com.sneakerstore.dto.request.ChangePasswordRequest;
import com.sneakerstore.dto.request.LoginRequest;
import com.sneakerstore.dto.response.LoginResponse;
import com.sneakerstore.dto.response.UserDto;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void changePassword(ChangePasswordRequest request);

    UserDto getCurrentUser();
}
