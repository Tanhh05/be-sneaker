package com.sneakerstore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseOrderRequest {
    private String purchaseCode;

    @NotNull(message = "Nhà cung cấp không được để trống")
    private Long supplierId;

    private Long employeeId;
    private String note;
    private boolean confirmImmediately = false;

    @NotEmpty(message = "Danh sách sản phẩm nhập không được để trống")
    @Valid
    private List<PurchaseItemRequest> items;

    @Data
    public static class PurchaseItemRequest {
        @NotNull(message = "Biến thể sản phẩm không được để trống")
        private Long productVariantId;

        @NotNull(message = "Số lượng nhập không được để trống")
        @Min(value = 1, message = "Số lượng nhập phải từ 1 trở lên")
        private Integer quantity;

        @NotNull(message = "Đơn giá nhập không được để trống")
        @DecimalMin(value = "0.0", message = "Đơn giá nhập không được âm")
        private BigDecimal unitPrice;
    }
}

