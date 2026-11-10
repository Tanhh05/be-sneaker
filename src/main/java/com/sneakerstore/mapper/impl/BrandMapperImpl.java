package com.sneakerstore.mapper.impl;

import com.sneakerstore.mapper.BrandMapper;

import com.sneakerstore.dto.response.BrandDto;
import com.sneakerstore.entity.Brand;
import org.springframework.stereotype.Component;

@Component
public class BrandMapperImpl implements BrandMapper {

    @Override
    public BrandDto toDto(Brand brand) {
        if (brand == null) {
            return null;
        }
        return BrandDto.builder()
                .id(brand.getId())
                .brandCode(brand.getBrandCode())
                .brandName(brand.getBrandName())
                .country(brand.getCountry())
                .logoUrl(brand.getLogoUrl())
                .description(brand.getDescription())
                .status(brand.getStatus())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }
}

