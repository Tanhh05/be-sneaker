package com.sneakerstore.service;

import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.ProductRequest;
import com.sneakerstore.dto.response.ProductDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    PageResponse<ProductDto> searchProducts(
            String keyword, Long brandId, Long categoryId, String status, Pageable pageable);

    List<ProductDto> getAllProducts();

    ProductDto getById(Long id);

    ProductDto create(ProductRequest request);

    ProductDto update(Long id, ProductRequest request);

    ProductDto discontinue(Long id);
}
