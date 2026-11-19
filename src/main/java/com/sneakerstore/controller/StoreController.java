package com.sneakerstore.controller;

import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.StoreOrderRequest;
import com.sneakerstore.dto.response.*;
import com.sneakerstore.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/brands")
    public ApiResponse<List<BrandDto>> getBrands() {
        return ApiResponse.ok(storeService.getActiveBrands());
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryDto>> getCategories() {
        return ApiResponse.ok(storeService.getActiveCategories());
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductDto>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.ok(storeService.getStoreProducts(keyword, brandId, categoryId, page, size));
    }

    @GetMapping("/products/featured")
    public ApiResponse<List<ProductDto>> getFeaturedProducts() {
        return ApiResponse.ok(storeService.getFeaturedProducts());
    }

    @GetMapping("/products/{id}")
    public ApiResponse<ProductDto> getProductDetail(@PathVariable Long id) {
        return ApiResponse.ok(storeService.getProductDetail(id));
    }

    @PostMapping("/checkout")
    public ApiResponse<StoreOrderResponse> checkout(@Valid @RequestBody StoreOrderRequest request) {
        return ApiResponse.ok("Đặt hàng thành công", storeService.checkout(request));
    }

    @GetMapping("/orders/track")
    public ApiResponse<SalesOrderDto> trackOrder(
            @RequestParam String code,
            @RequestParam(required = false) String phone) {
        return ApiResponse.ok(storeService.trackOrder(code, phone));
    }

    @GetMapping("/my-orders")
    public ApiResponse<List<SalesOrderDto>> getMyOrders(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email) {
        return ApiResponse.ok(storeService.getMyOrders(phone, email));
    }

    @GetMapping("/my-orders/{orderCode}")
    public ApiResponse<SalesOrderDto> getMyOrderDetail(
            @PathVariable String orderCode,
            @RequestParam(required = false) String phone) {
        return ApiResponse.ok(storeService.getMyOrderDetail(orderCode, phone));
    }

    @PostMapping("/my-orders/{orderCode}/cancel")
    public ApiResponse<SalesOrderDto> cancelMyOrder(
            @PathVariable String orderCode,
            @RequestBody(required = false) Map<String, String> payload) {
        String reason = payload != null ? payload.get("reason") : null;
        String phone = payload != null ? payload.get("phone") : null;
        return ApiResponse.ok("Hủy đơn hàng thành công", storeService.cancelMyOrder(orderCode, reason, phone));
    }
}
