package com.sneakerstore.mapper;

import com.sneakerstore.dto.response.BrandDto;
import com.sneakerstore.entity.Brand;

public interface BrandMapper {
    BrandDto toDto(Brand brand);
}
