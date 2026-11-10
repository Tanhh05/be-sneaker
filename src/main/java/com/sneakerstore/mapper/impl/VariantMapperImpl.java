package com.sneakerstore.mapper.impl;

import com.sneakerstore.mapper.VariantMapper;

import com.sneakerstore.entity.Product;
import com.sneakerstore.dto.response.ColorDto;
import com.sneakerstore.dto.response.ProductVariantDto;
import com.sneakerstore.dto.response.SizeDto;
import com.sneakerstore.entity.Color;
import com.sneakerstore.entity.ProductVariant;
import com.sneakerstore.entity.ShoeSize;
import org.springframework.stereotype.Component;

@Component
public class VariantMapperImpl implements VariantMapper {

    @Override
    public SizeDto toSizeDto(ShoeSize size) {
        if (size == null) {
            return null;
        }
        return SizeDto.builder()
                .id(size.getId())
                .sizeValue(size.getSizeValue())
                .sizeNumber(size.getSizeNumber())
                .description(size.getDescription())
                .status(size.getStatus())
                .build();
    }

    @Override
    public ColorDto toColorDto(Color color) {
        if (color == null) {
            return null;
        }
        return ColorDto.builder()
                .id(color.getId())
                .colorCode(color.getColorCode())
                .colorName(color.getColorName())
                .hexCode(color.getHexCode())
                .status(color.getStatus())
                .build();
    }

    @Override
    public ProductVariantDto toVariantDto(ProductVariant variant) {
        if (variant == null) {
            return null;
        }
        Product p = variant.getProduct();
        return ProductVariantDto.builder()
                .id(variant.getId())
                .productId(p != null ? p.getId() : null)
                .productCode(p != null ? p.getProductCode() : null)
                .productName(p != null ? p.getProductName() : null)
                .brandName(p != null && p.getBrand() != null ? p.getBrand().getBrandName() : null)
                .categoryName(p != null && p.getCategory() != null ? p.getCategory().getCategoryName() : null)
                .thumbnailUrl(p != null ? p.getThumbnailUrl() : null)
                .sizeId(variant.getSize() != null ? variant.getSize().getId() : null)
                .sizeValue(variant.getSize() != null ? variant.getSize().getSizeValue() : null)
                .colorId(variant.getColor() != null ? variant.getColor().getId() : null)
                .colorCode(variant.getColor() != null ? variant.getColor().getColorCode() : null)
                .colorName(variant.getColor() != null ? variant.getColor().getColorName() : null)
                .hexCode(variant.getColor() != null ? variant.getColor().getHexCode() : null)
                .sku(variant.getSku())
                .importPrice(variant.getImportPrice())
                .salePrice(variant.getSalePrice())
                .availableQuantity(variant.getAvailableQuantity())
                .status(variant.getStatus())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }
}

