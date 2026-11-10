package com.sneakerstore.controller;

import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.response.PurchaseOrderDto;
import com.sneakerstore.dto.request.PurchaseOrderRequest;
import com.sneakerstore.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<PageResponse<PurchaseOrderDto>> getPurchases(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(purchaseService.searchPurchases(
                keyword, supplierId, status, fromDate, toDate,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<PurchaseOrderDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(purchaseService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<PurchaseOrderDto> create(@Valid @RequestBody PurchaseOrderRequest request) {
        return ApiResponse.ok("Tạo phiếu nhập thành công", purchaseService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<PurchaseOrderDto> update(
            @PathVariable Long id, @Valid @RequestBody PurchaseOrderRequest request) {
        return ApiResponse.ok("Cập nhật phiếu nhập thành công", purchaseService.update(id, request));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<PurchaseOrderDto> confirm(@PathVariable Long id) {
        return ApiResponse.ok("Xác nhận phiếu nhập thành công", purchaseService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<PurchaseOrderDto> cancel(@PathVariable Long id) {
        return ApiResponse.ok("Đã hủy phiếu nhập", purchaseService.cancel(id));
    }
}

