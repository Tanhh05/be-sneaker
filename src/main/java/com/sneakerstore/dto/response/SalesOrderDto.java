package com.sneakerstore.dto.response;

import com.sneakerstore.dto.response.PaymentDto;
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
public class SalesOrderDto {
    private Long id;
    private String orderCode;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String customerPhone;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private String status;
    private String note;
    private List<SalesOrderDetailDto> details;
    private List<PaymentDto> payments;
    private List<OrderStatusHistoryDto> histories;
    private LocalDateTime createdAt;
}

