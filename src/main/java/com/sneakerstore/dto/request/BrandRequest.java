package com.sneakerstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandRequest {
    @NotBlank(message = "Mã thương hiệu không được để trống")
    @Size(max = 30, message = "Mã thương hiệu tối đa 30 ký tự")
    private String brandCode;

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 100, message = "Tên thương hiệu tối đa 100 ký tự")
    private String brandName;

    private String country;
    private String logoUrl;
    private String description;
    private String status = "ACTIVE";
}

