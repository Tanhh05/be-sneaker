package com.sneakerstore.mapper;

import com.sneakerstore.dto.response.ColorDto;
import com.sneakerstore.dto.response.ProductVariantDto;
import com.sneakerstore.dto.response.SizeDto;
import com.sneakerstore.entity.Color;
import com.sneakerstore.entity.ProductVariant;
import com.sneakerstore.entity.ShoeSize;

public interface VariantMapper {
    SizeDto toSizeDto(ShoeSize size);
    ColorDto toColorDto(Color color);
    ProductVariantDto toVariantDto(ProductVariant variant);
}
