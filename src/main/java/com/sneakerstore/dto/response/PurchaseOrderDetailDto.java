package com.sneakerstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDetailDto {
    private Long id;
    private Long productVariantId;
    private Long productId;
    private String productName;
    private String sku;
    private String sizeValue;
    private String colorName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}

