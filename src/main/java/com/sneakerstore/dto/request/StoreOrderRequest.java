package com.sneakerstore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreOrderRequest {

    @NotBlank(message = "Họ tên người nhận không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String customerPhone;

    private String customerEmail;

    @NotBlank(message = "Địa chỉ nhận hàng không được để trống")
    private String shippingAddress;

    private String paymentMethod; // CASH (COD) hoặc BANK_TRANSFER
    private String note;

    @NotEmpty(message = "Giỏ hàng phải có ít nhất 1 sản phẩm")
    @Valid
    private List<StoreOrderItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreOrderItemRequest {
        @NotNull(message = "Mã biến thể không được để trống")
        private Long variantId;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải từ 1 trở lên")
        private Integer quantity;
    }
}
