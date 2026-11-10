package com.sneakerstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequest {
    private String customerCode;

    @NotBlank(message = "Họ tên khách hàng không được để trống")
    @Size(max = 100, message = "Họ tên khách hàng tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Số điện thoại khách hàng không được để trống")
    private String phone;

    private String email;
    private String gender = "MALE";
    private LocalDate dateOfBirth;
    private String address;
    private String status = "ACTIVE";
}

