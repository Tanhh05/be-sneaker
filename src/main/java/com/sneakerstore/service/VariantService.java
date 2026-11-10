package com.sneakerstore.service;

import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.ProductVariantRequest;
import com.sneakerstore.dto.response.ProductVariantDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VariantService {

    PageResponse<ProductVariantDto> searchVariants(
            String keyword, Long productId, Long sizeId, Long colorId, String status, Pageable pageable);

    List<ProductVariantDto> getAllVariants();

    List<ProductVariantDto> getVariantsByProduct(Long productId);

    ProductVariantDto getVariantById(Long id);

    ProductVariantDto createVariant(ProductVariantRequest request);

    ProductVariantDto updateVariant(Long id, ProductVariantRequest request);
}
