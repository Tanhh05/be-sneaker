package com.sneakerstore.service.impl;

import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.util.SecurityUtils;
import com.sneakerstore.dto.request.ColorRequest;
import com.sneakerstore.dto.response.ColorDto;
import com.sneakerstore.entity.Color;
import com.sneakerstore.mapper.VariantMapper;
import com.sneakerstore.repository.ColorRepository;
import com.sneakerstore.service.ColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColorServiceImpl implements ColorService {

    private final ColorRepository colorRepository;
    private final VariantMapper variantMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ColorDto> getAllColors() {
        return colorRepository.findAllByOrderByIdAsc().stream()
                .map(variantMapper::toColorDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ColorDto createColor(ColorRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra trùng mã màu và tên màu)
        String code = request.getColorCode().trim().toUpperCase();
        String name = request.getColorName().trim();
        if (colorRepository.existsByColorCodeIgnoreCase(code)) {
            throw new BusinessException("Mã màu đã tồn tại: " + code);
        }
        if (colorRepository.existsByColorNameIgnoreCase(name)) {
            throw new BusinessException("Tên màu đã tồn tại: " + name);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Khởi tạo màu sắc)
        String actor = SecurityUtils.getCurrentUsername();
        Color color = Color.builder()
                .colorCode(code)
                .colorName(name)
                .hexCode(request.getHexCode().trim())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                // Bước 4: Ghi vết / Audit
                .createdBy(actor)
                .updatedBy(actor)
                .build();

        // Bước 5: Output Mapping
        return variantMapper.toColorDto(colorRepository.save(color));
    }

    @Override
    @Transactional
    public ColorDto updateColor(Long id, ColorRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại bản ghi và trùng mã/tên màu)
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy màu sắc với ID: " + id));
        String code = request.getColorCode().trim().toUpperCase();
        String name = request.getColorName().trim();
        if (!color.getColorCode().equalsIgnoreCase(code) && colorRepository.existsByColorCodeIgnoreCase(code)) {
            throw new BusinessException("Mã màu đã tồn tại: " + code);
        }
        if (!color.getColorName().equalsIgnoreCase(name) && colorRepository.existsByColorNameIgnoreCase(name)) {
            throw new BusinessException("Tên màu đã tồn tại: " + name);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Cập nhật màu sắc)
        color.setColorCode(code);
        color.setColorName(name);
        color.setHexCode(request.getHexCode().trim());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            color.setStatus(request.getStatus());
        }

        // Bước 4: Ghi vết / Audit
        color.setUpdatedBy(SecurityUtils.getCurrentUsername());

        // Bước 5: Output Mapping
        return variantMapper.toColorDto(colorRepository.save(color));
    }
}

