package com.sneakerstore.service;

import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.StoreOrderRequest;
import com.sneakerstore.dto.response.BrandDto;
import com.sneakerstore.dto.response.CategoryDto;
import com.sneakerstore.dto.response.ProductDto;
import com.sneakerstore.dto.response.SalesOrderDto;
import com.sneakerstore.dto.response.StoreOrderResponse;

import java.util.List;

public interface StoreService {
    List<BrandDto> getActiveBrands();
    List<CategoryDto> getActiveCategories();
    PageResponse<ProductDto> getStoreProducts(String keyword, Long brandId, Long categoryId, int page, int size);
    List<ProductDto> getFeaturedProducts();
    ProductDto getProductDetail(Long id);
    StoreOrderResponse checkout(StoreOrderRequest request);
    SalesOrderDto trackOrder(String code, String phone);
}
