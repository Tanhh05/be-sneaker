package com.sneakerstore.controller;

import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.dto.response.ColorDto;
import com.sneakerstore.dto.request.ColorRequest;
import com.sneakerstore.service.ColorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
public class ColorController {

    private final ColorService colorService;

    @GetMapping
    public ApiResponse<List<ColorDto>> getColors() {
        return ApiResponse.ok(colorService.getAllColors());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<ColorDto> createColor(@Valid @RequestBody ColorRequest request) {
        return ApiResponse.ok("Thêm màu sắc thành công", colorService.createColor(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<ColorDto> updateColor(@PathVariable Long id, @Valid @RequestBody ColorRequest request) {
        return ApiResponse.ok("Cập nhật màu sắc thành công", colorService.updateColor(id, request));
    }
}

