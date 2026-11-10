package com.sneakerstore.mapper;

import com.sneakerstore.dto.response.ProductDto;
import com.sneakerstore.entity.Product;

public interface ProductMapper {
    ProductDto toSummaryDto(Product product);
}
