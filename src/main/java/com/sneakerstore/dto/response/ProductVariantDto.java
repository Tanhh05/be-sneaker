package com.sneakerstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDto {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private String brandName;
    private String categoryName;
    private String thumbnailUrl;
    private Long sizeId;
    private String sizeValue;
    private Long colorId;
    private String colorCode;
    private String colorName;
    private String hexCode;
    private String sku;
    private BigDecimal importPrice;
    private BigDecimal salePrice;
    private Integer availableQuantity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

