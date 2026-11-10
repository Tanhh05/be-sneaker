package com.sneakerstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Mã danh mục không được để trống")
    @Size(max = 30, message = "Mã danh mục tối đa 30 ký tự")
    private String categoryCode;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    private String categoryName;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    private String status = "ACTIVE";
}

