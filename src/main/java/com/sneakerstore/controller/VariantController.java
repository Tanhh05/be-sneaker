package com.sneakerstore.controller;

import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.response.ProductVariantDto;
import com.sneakerstore.dto.request.ProductVariantRequest;
import com.sneakerstore.service.VariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class VariantController {

    private final VariantService variantService;

    @GetMapping
    public ApiResponse<PageResponse<ProductVariantDto>> getVariants(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long sizeId,
            @RequestParam(required = false) Long colorId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(variantService.searchVariants(
                keyword, productId, sizeId, colorId, status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"))));
    }

    @GetMapping("/all")
    public ApiResponse<List<ProductVariantDto>> getAllVariants() {
        return ApiResponse.ok(variantService.getAllVariants());
    }

    @GetMapping("/by-product/{productId}")
    public ApiResponse<List<ProductVariantDto>> getVariantsByProduct(@PathVariable Long productId) {
        return ApiResponse.ok(variantService.getVariantsByProduct(productId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductVariantDto> getVariantById(@PathVariable Long id) {
        return ApiResponse.ok(variantService.getVariantById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<ProductVariantDto> createVariant(@Valid @RequestBody ProductVariantRequest request) {
        return ApiResponse.ok("Thêm biến thể sản phẩm thành công", variantService.createVariant(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<ProductVariantDto> updateVariant(
            @PathVariable Long id, @Valid @RequestBody ProductVariantRequest request) {
        return ApiResponse.ok("Cập nhật biến thể sản phẩm thành công", variantService.updateVariant(id, request));
    }
}
