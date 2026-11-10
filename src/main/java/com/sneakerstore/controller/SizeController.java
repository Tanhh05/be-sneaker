package com.sneakerstore.controller;

import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.dto.response.SizeDto;
import com.sneakerstore.dto.request.SizeRequest;
import com.sneakerstore.service.SizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sizes")
@RequiredArgsConstructor
public class SizeController {

    private final SizeService sizeService;

    @GetMapping
    public ApiResponse<List<SizeDto>> getSizes() {
        return ApiResponse.ok(sizeService.getAllSizes());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<SizeDto> createSize(@Valid @RequestBody SizeRequest request) {
        return ApiResponse.ok("Thêm Size thành công", sizeService.createSize(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<SizeDto> updateSize(@PathVariable Long id, @Valid @RequestBody SizeRequest request) {
        return ApiResponse.ok("Cập nhật Size thành công", sizeService.updateSize(id, request));
    }
}

