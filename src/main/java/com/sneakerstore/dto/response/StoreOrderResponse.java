package com.sneakerstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreOrderResponse {
    private Long orderId;
    private String orderCode;
    private LocalDateTime orderDate;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String status;
    private String qrPaymentUrl;
    private String bankInfo;
    private String bankAccount;
    private String bankName;
    private String accountName;
    private String note;
    private List<SalesOrderDetailDto> items;
}
