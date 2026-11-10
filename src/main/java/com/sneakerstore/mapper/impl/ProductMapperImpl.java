package com.sneakerstore.mapper.impl;

import com.sneakerstore.mapper.ProductMapper;

import com.sneakerstore.dto.response.ProductDto;
import com.sneakerstore.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductDto toSummaryDto(Product product) {
        if (product == null) {
            return null;
        }
        return ProductDto.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .productName(product.getProductName())
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getBrandName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                .material(product.getMaterial())
                .description(product.getDescription())
                .thumbnailUrl(product.getThumbnailUrl())
                .importPrice(product.getImportPrice())
                .salePrice(product.getSalePrice())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}

