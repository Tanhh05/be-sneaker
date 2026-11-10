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
public class PurchaseOrderDto {
    private Long id;
    private String purchaseCode;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime purchaseDate;
    private BigDecimal totalAmount;
    private String status;
    private String note;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private List<PurchaseOrderDetailDto> details;
    private LocalDateTime createdAt;
}

