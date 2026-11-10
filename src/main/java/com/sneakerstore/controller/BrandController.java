package com.sneakerstore.controller;

import com.sneakerstore.dto.response.BrandDto;
import com.sneakerstore.dto.request.BrandRequest;
import com.sneakerstore.service.BrandService;
import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ApiResponse<PageResponse<BrandDto>> getBrands(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(brandService.searchBrands(
                keyword, status, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"))));
    }

    @GetMapping("/all")
    public ApiResponse<List<BrandDto>> getAllBrands() {
        return ApiResponse.ok(brandService.getAllBrands());
    }

    @GetMapping("/{id}")
    public ApiResponse<BrandDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(brandService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<BrandDto> create(@Valid @RequestBody BrandRequest request) {
        return ApiResponse.ok("Thêm thương hiệu thành công", brandService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<BrandDto> update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return ApiResponse.ok("Cập nhật thương hiệu thành công", brandService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<BrandDto> deactivate(@PathVariable Long id) {
        return ApiResponse.ok("Đã ngừng sử dụng thương hiệu", brandService.deactivate(id));
    }
}

