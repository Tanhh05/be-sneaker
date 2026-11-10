package com.sneakerstore.service.impl;

import com.sneakerstore.dto.request.BrandRequest;
import com.sneakerstore.dto.response.BrandDto;
import com.sneakerstore.entity.Brand;
import com.sneakerstore.mapper.BrandMapper;
import com.sneakerstore.repository.BrandRepository;
import com.sneakerstore.service.BrandService;
import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BrandDto> searchBrands(String keyword, String status, Pageable pageable) {
        Page<BrandDto> page = brandRepository.searchBrands(keyword, status, pageable)
                .map(brandMapper::toDto);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandDto> getAllBrands() {
        return brandRepository.findAllByOrderByBrandNameAsc().stream()
                .map(brandMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BrandDto getById(Long id) {
        // Bước 1: Validation nghiệp vụ
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));
        // Bước 5: Output Mapping
        return brandMapper.toDto(brand);
    }

    @Override
    @Transactional
    public BrandDto create(BrandRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra trùng mã và tên thương hiệu)
        String code = request.getBrandCode().trim().toUpperCase();
        String name = request.getBrandName().trim();
        if (brandRepository.existsByBrandCodeIgnoreCase(code)) {
            throw new BusinessException("Mã thương hiệu đã tồn tại: " + code);
        }
        if (brandRepository.existsByBrandNameIgnoreCase(name)) {
            throw new BusinessException("Tên thương hiệu đã tồn tại: " + name);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Khởi tạo thương hiệu)
        String actor = SecurityUtils.getCurrentUsername();
        Brand brand = Brand.builder()
                .brandCode(code)
                .brandName(name)
                .country(request.getCountry())
                .logoUrl(request.getLogoUrl())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                // Bước 4: Ghi vết / Audit
                .createdBy(actor)
                .updatedBy(actor)
                .build();

        // Bước 5: Output Mapping
        return brandMapper.toDto(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandDto update(Long id, BrandRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại bản ghi và trùng mã/tên thương hiệu)
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));

        String code = request.getBrandCode().trim().toUpperCase();
        String name = request.getBrandName().trim();
        if (!brand.getBrandCode().equalsIgnoreCase(code) && brandRepository.existsByBrandCodeIgnoreCase(code)) {
            throw new BusinessException("Mã thương hiệu đã tồn tại: " + code);
        }
        if (!brand.getBrandName().equalsIgnoreCase(name) && brandRepository.existsByBrandNameIgnoreCase(name)) {
            throw new BusinessException("Tên thương hiệu đã tồn tại: " + name);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Cập nhật thông tin)
        brand.setBrandCode(code);
        brand.setBrandName(name);
        brand.setCountry(request.getCountry());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setDescription(request.getDescription());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            brand.setStatus(request.getStatus());
        }

        // Bước 4: Ghi vết / Audit
        brand.setUpdatedBy(SecurityUtils.getCurrentUsername());

        // Bước 5: Output Mapping
        return brandMapper.toDto(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandDto deactivate(Long id) {
        // Bước 1: Validation nghiệp vụ
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));
        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Ngừng sử dụng)
        brand.setStatus("INACTIVE");
        // Bước 4: Ghi vết / Audit
        brand.setUpdatedBy(SecurityUtils.getCurrentUsername());
        // Bước 5: Output Mapping
        return brandMapper.toDto(brandRepository.save(brand));
    }
}

