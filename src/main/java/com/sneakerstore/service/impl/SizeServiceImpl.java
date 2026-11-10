package com.sneakerstore.service.impl;

import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.util.SecurityUtils;
import com.sneakerstore.dto.request.SizeRequest;
import com.sneakerstore.dto.response.SizeDto;
import com.sneakerstore.entity.ShoeSize;
import com.sneakerstore.mapper.VariantMapper;
import com.sneakerstore.repository.SizeRepository;
import com.sneakerstore.service.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;
    private final VariantMapper variantMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SizeDto> getAllSizes() {
        return sizeRepository.findAllByOrderBySizeNumberAsc().stream()
                .map(variantMapper::toSizeDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SizeDto createSize(SizeRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra trùng lặp giá trị Size)
        String val = request.getSizeValue().trim();
        if (sizeRepository.existsBySizeValueIgnoreCase(val)) {
            throw new BusinessException("Size đã tồn tại: " + val);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Khởi tạo Size)
        String actor = SecurityUtils.getCurrentUsername();
        ShoeSize size = ShoeSize.builder()
                .sizeValue(val)
                .sizeNumber(request.getSizeNumber())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                // Bước 4: Ghi vết / Audit
                .createdBy(actor)
                .updatedBy(actor)
                .build();

        // Bước 5: Output Mapping
        return variantMapper.toSizeDto(sizeRepository.save(size));
    }

    @Override
    @Transactional
    public SizeDto updateSize(Long id, SizeRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại bản ghi và trùng giá trị Size)
        ShoeSize size = sizeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Size với ID: " + id));
        String val = request.getSizeValue().trim();
        if (!size.getSizeValue().equalsIgnoreCase(val) && sizeRepository.existsBySizeValueIgnoreCase(val)) {
            throw new BusinessException("Size đã tồn tại: " + val);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Cập nhật Size)
        size.setSizeValue(val);
        size.setSizeNumber(request.getSizeNumber());
        size.setDescription(request.getDescription());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            size.setStatus(request.getStatus());
        }

        // Bước 4: Ghi vết / Audit
        size.setUpdatedBy(SecurityUtils.getCurrentUsername());

        // Bước 5: Output Mapping
        return variantMapper.toSizeDto(sizeRepository.save(size));
    }
}

