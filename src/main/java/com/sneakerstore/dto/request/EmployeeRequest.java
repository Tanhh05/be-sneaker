package com.sneakerstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeRequest {
    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 30, message = "Mã nhân viên tối đa 30 ký tự")
    private String employeeCode;

    @NotBlank(message = "Họ tên nhân viên không được để trống")
    @Size(max = 100, message = "Họ tên nhân viên tối đa 100 ký tự")
    private String fullName;

    private LocalDate dateOfBirth;
    private String gender = "MALE";

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String email;
    private String address;

    @NotBlank(message = "Chức vụ không được để trống")
    private String position;

    @NotNull(message = "Ngày vào làm không được để trống")
    private LocalDate hireDate;

    private Long userId;

    private String status = "ACTIVE";
}

