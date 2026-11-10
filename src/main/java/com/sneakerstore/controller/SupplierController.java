package com.sneakerstore.controller;

import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.response.SupplierDto;
import com.sneakerstore.dto.request.SupplierRequest;
import com.sneakerstore.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ApiResponse<PageResponse<SupplierDto>> getSuppliers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(supplierService.searchSuppliers(
                keyword, status, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"))));
    }

    @GetMapping("/all")
    public ApiResponse<List<SupplierDto>> getAllSuppliers() {
        return ApiResponse.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    public ApiResponse<SupplierDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(supplierService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<SupplierDto> create(@Valid @RequestBody SupplierRequest request) {
        return ApiResponse.ok("Thêm nhà cung cấp thành công", supplierService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<SupplierDto> update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return ApiResponse.ok("Cập nhật nhà cung cấp thành công", supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<SupplierDto> deactivate(@PathVariable Long id) {
        return ApiResponse.ok("Đã ngừng hợp tác với nhà cung cấp", supplierService.deactivate(id));
    }
}

