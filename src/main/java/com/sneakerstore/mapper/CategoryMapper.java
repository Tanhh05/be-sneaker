package com.sneakerstore.mapper;

import com.sneakerstore.dto.response.CategoryDto;
import com.sneakerstore.entity.Category;

public interface CategoryMapper {
    CategoryDto toDto(Category category);
}
