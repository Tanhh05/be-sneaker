package com.sneakerstore.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantRequest {
    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;

    @NotNull(message = "Size không được để trống")
    private Long sizeId;

    @NotNull(message = "Màu sắc không được để trống")
    private Long colorId;

    @NotBlank(message = "Mã SKU không được để trống")
    private String sku;

    @NotNull(message = "Giá nhập không được để trống")
    @DecimalMin(value = "0.0", message = "Giá nhập phải lớn hơn hoặc bằng 0")
    private BigDecimal importPrice;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.0", message = "Giá bán phải lớn hơn hoặc bằng 0")
    private BigDecimal salePrice;

    @Min(value = 0, message = "Số lượng có sẵn không được âm")
    private Integer availableQuantity = 0;

    private String status = "ACTIVE";
}

