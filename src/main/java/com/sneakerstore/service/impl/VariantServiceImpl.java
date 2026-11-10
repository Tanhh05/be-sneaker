package com.sneakerstore.service.impl;

import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.common.util.SecurityUtils;
import com.sneakerstore.entity.Product;
import com.sneakerstore.repository.ProductRepository;
import com.sneakerstore.dto.request.ProductVariantRequest;
import com.sneakerstore.dto.response.ProductVariantDto;
import com.sneakerstore.entity.Color;
import com.sneakerstore.entity.ProductVariant;
import com.sneakerstore.entity.ShoeSize;
import com.sneakerstore.mapper.VariantMapper;
import com.sneakerstore.repository.ColorRepository;
import com.sneakerstore.repository.ProductVariantRepository;
import com.sneakerstore.repository.SizeRepository;
import com.sneakerstore.service.VariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {

    private final ProductVariantRepository variantRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;
    private final ProductRepository productRepository;
    private final VariantMapper variantMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductVariantDto> searchVariants(
            String keyword, Long productId, Long sizeId, Long colorId, String status, Pageable pageable) {
        Page<ProductVariantDto> page = variantRepository
                .searchVariants(keyword, productId, sizeId, colorId, status, pageable)
                .map(variantMapper::toVariantDto);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantDto> getAllVariants() {
        return variantRepository.findAllByOrderByIdAsc().stream()
                .map(variantMapper::toVariantDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantDto> getVariantsByProduct(Long productId) {
        return variantRepository.findByProductIdOrderByIdAsc(productId).stream()
                .map(variantMapper::toVariantDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantDto getVariantById(Long id) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại biến thể SKU)
        ProductVariant pv = variantRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể với ID: " + id));
        // Bước 5: Output Mapping
        return variantMapper.toVariantDto(pv);
    }

    @Override
    @Transactional
    public ProductVariantDto createVariant(ProductVariantRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra trùng mã SKU, tổ hợp Product+Size+Color và các khóa ngoại)
        String sku = request.getSku().trim().toUpperCase();
        if (variantRepository.existsBySkuIgnoreCase(sku)) {
            throw new BusinessException("Mã SKU đã tồn tại: " + sku);
        }
        if (variantRepository.existsByProductIdAndSizeIdAndColorId(
                request.getProductId(), request.getSizeId(), request.getColorId())) {
            throw new BusinessException("Biến thể với Size và Màu sắc này đã tồn tại cho sản phẩm");
        }

        Product product = productRepository.findWithDetailsById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + request.getProductId()));
        ShoeSize size = sizeRepository.findById(request.getSizeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Size với ID: " + request.getSizeId()));
        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy màu sắc với ID: " + request.getColorId()));

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Khởi tạo biến thể sản phẩm SKU)
        String actor = SecurityUtils.getCurrentUsername();
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .size(size)
                .color(color)
                .sku(sku)
                .importPrice(request.getImportPrice())
                .salePrice(request.getSalePrice())
                .availableQuantity(request.getAvailableQuantity() != null ? request.getAvailableQuantity() : 0)
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                // Bước 4: Ghi vết / Audit
                .createdBy(actor)
                .updatedBy(actor)
                .build();

        // Bước 5: Output Mapping
        return variantMapper.toVariantDto(variantRepository.save(variant));
    }

    @Override
    @Transactional
    public ProductVariantDto updateVariant(Long id, ProductVariantRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại bản ghi, trùng SKU và tổ hợp Product+Size+Color)
        ProductVariant variant = variantRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể với ID: " + id));

        String sku = request.getSku().trim().toUpperCase();
        if (!variant.getSku().equalsIgnoreCase(sku) && variantRepository.existsBySkuIgnoreCase(sku)) {
            throw new BusinessException("Mã SKU đã tồn tại: " + sku);
        }

        boolean comboChanged = !variant.getProduct().getId().equals(request.getProductId())
                || !variant.getSize().getId().equals(request.getSizeId())
                || !variant.getColor().getId().equals(request.getColorId());

        if (comboChanged && variantRepository.existsByProductIdAndSizeIdAndColorId(
                request.getProductId(), request.getSizeId(), request.getColorId())) {
            throw new BusinessException("Biến thể với Size và Màu sắc này đã tồn tại cho sản phẩm");
        }

        Product product = productRepository.findWithDetailsById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + request.getProductId()));
        ShoeSize size = sizeRepository.findById(request.getSizeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Size với ID: " + request.getSizeId()));
        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy màu sắc với ID: " + request.getColorId()));

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Cập nhật thông tin SKU)
        variant.setProduct(product);
        variant.setSize(size);
        variant.setColor(color);
        variant.setSku(sku);
        variant.setImportPrice(request.getImportPrice());
        variant.setSalePrice(request.getSalePrice());
        if (request.getAvailableQuantity() != null) {
            variant.setAvailableQuantity(request.getAvailableQuantity());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            variant.setStatus(request.getStatus());
        }

        // Bước 4: Ghi vết / Audit
        variant.setUpdatedBy(SecurityUtils.getCurrentUsername());

        // Bước 5: Output Mapping
        return variantMapper.toVariantDto(variantRepository.save(variant));
    }
}

