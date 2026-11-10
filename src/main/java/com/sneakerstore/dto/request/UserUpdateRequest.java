package com.sneakerstore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    private String email;

    private String phone;

    private String status;

    private String newPassword;

    @NotEmpty(message = "Phải chọn ít nhất một vai trò")
    private List<String> roles;
}

