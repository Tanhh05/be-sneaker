package com.sneakerstore.service;

import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.CreateReviewRequest;
import com.sneakerstore.dto.request.StoreOrderRequest;
import com.sneakerstore.dto.response.*;

import java.util.List;

public interface StoreService {
    List<BrandDto> getActiveBrands();
    List<CategoryDto> getActiveCategories();
    PageResponse<ProductDto> getStoreProducts(String keyword, Long brandId, Long categoryId, int page, int size);
    List<ProductDto> getFeaturedProducts();
    ProductDto getBestSellerProduct();
    ProductDto getProductDetail(Long id);
    StoreOrderResponse checkout(StoreOrderRequest request);
    SalesOrderDto trackOrder(String code, String phone);
    List<SalesOrderDto> getMyOrders(String phone, String email);
    SalesOrderDto getMyOrderDetail(String orderCode, String phone);
    SalesOrderDto cancelMyOrder(String orderCode, String reason, String phone);
    ProductReviewSummaryDto getProductReviews(Long productId);
    ProductReviewDto submitProductReview(Long productId, CreateReviewRequest request);
}
