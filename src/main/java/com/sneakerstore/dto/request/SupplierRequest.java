package com.sneakerstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierRequest {
    @NotBlank(message = "Mã nhà cung cấp không được để trống")
    @Size(max = 30, message = "Mã nhà cung cấp tối đa 30 ký tự")
    private String supplierCode;

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Size(max = 200, message = "Tên nhà cung cấp tối đa 200 ký tự")
    private String supplierName;

    private String contactPerson;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String email;
    private String address;
    private String taxCode;
    private String status = "ACTIVE";
}

