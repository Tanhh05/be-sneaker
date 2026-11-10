package com.sneakerstore.service.impl;

import com.sneakerstore.entity.Brand;
import com.sneakerstore.repository.BrandRepository;
import com.sneakerstore.entity.Category;
import com.sneakerstore.repository.CategoryRepository;
import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.common.util.SecurityUtils;
import com.sneakerstore.dto.request.ProductRequest;
import com.sneakerstore.dto.response.ProductDto;
import com.sneakerstore.entity.Product;
import com.sneakerstore.entity.ProductImage;
import com.sneakerstore.mapper.ProductMapper;
import com.sneakerstore.repository.ProductImageRepository;
import com.sneakerstore.repository.ProductRepository;
import com.sneakerstore.service.ProductService;
import com.sneakerstore.service.VariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final VariantService variantService;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> searchProducts(
            String keyword, Long brandId, Long categoryId, String status, Pageable pageable) {
        Page<ProductDto> page = productRepository
                .searchProducts(keyword, brandId, categoryId, status, pageable)
                .map(productMapper::toSummaryDto);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAllByOrderByIdDesc().stream()
                .map(productMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại sản phẩm)
        Product product = productRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        // Bước 3 & 5: Tải danh sách ảnh + biến thể SKU và map sang ProductDto
        ProductDto dto = productMapper.toSummaryDto(product);
        List<String> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(id)
                .stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        dto.setImageUrls(images);
        dto.setVariants(variantService.getVariantsByProduct(id));
        return dto;
    }

    @Override
    @Transactional
    public ProductDto create(ProductRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra trùng mã SP và khóa ngoại Brand/Category)
        String code = request.getProductCode().trim().toUpperCase();
        if (productRepository.existsByProductCodeIgnoreCase(code)) {
            throw new BusinessException("Mã sản phẩm đã tồn tại: " + code);
        }
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + request.getBrandId()));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Khởi tạo sản phẩm và lưu danh sách ảnh)
        String actor = SecurityUtils.getCurrentUsername();
        Product product = Product.builder()
                .productCode(code)
                .productName(request.getProductName().trim())
                .brand(brand)
                .category(category)
                .material(request.getMaterial())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .importPrice(request.getImportPrice())
                .salePrice(request.getSalePrice())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                // Bước 4: Ghi vết / Audit
                .createdBy(actor)
                .updatedBy(actor)
                .build();

        Product saved = productRepository.save(product);
        saveImages(saved, request.getThumbnailUrl(), request.getImageUrls(), actor);

        // Bước 5: Output Mapping
        return getById(saved.getId());
    }

    @Override
    @Transactional
    public ProductDto update(Long id, ProductRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại bản ghi, trùng mã SP và khóa ngoại Brand/Category)
        Product product = productRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        String code = request.getProductCode().trim().toUpperCase();
        if (!product.getProductCode().equalsIgnoreCase(code) && productRepository.existsByProductCodeIgnoreCase(code)) {
            throw new BusinessException("Mã sản phẩm đã tồn tại: " + code);
        }
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + request.getBrandId()));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Cập nhật thông tin SP và danh sách hình ảnh)
        String actor = SecurityUtils.getCurrentUsername();
        product.setProductCode(code);
        product.setProductName(request.getProductName().trim());
        product.setBrand(brand);
        product.setCategory(category);
        product.setMaterial(request.getMaterial());
        product.setDescription(request.getDescription());
        product.setThumbnailUrl(request.getThumbnailUrl());
        product.setImportPrice(request.getImportPrice());
        product.setSalePrice(request.getSalePrice());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            product.setStatus(request.getStatus());
        }

        // Bước 4: Ghi vết / Audit
        product.setUpdatedBy(actor);

        Product saved = productRepository.save(product);
        if (request.getImageUrls() != null) {
            productImageRepository.deleteByProductId(saved.getId());
            saveImages(saved, request.getThumbnailUrl(), request.getImageUrls(), actor);
        }

        // Bước 5: Output Mapping
        return getById(saved.getId());
    }

    @Override
    @Transactional
    public ProductDto discontinue(Long id) {
        // Bước 1: Validation nghiệp vụ
        Product product = productRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Ngừng kinh doanh)
        product.setStatus("INACTIVE");
        // Bước 4: Ghi vết / Audit
        product.setUpdatedBy(SecurityUtils.getCurrentUsername());
        // Bước 5: Output Mapping
        return productMapper.toSummaryDto(productRepository.save(product));
    }

    private void saveImages(Product product, String thumbnailUrl, List<String> imageUrls, String actor) {
        List<ProductImage> toSave = new ArrayList<>();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            int order = 1;
            for (String url : imageUrls) {
                if (url != null && !url.isBlank()) {
                    toSave.add(ProductImage.builder()
                            .product(product)
                            .imageUrl(url.trim())
                            .isPrimary(order == 1 ? 1 : 0)
                            .displayOrder(order++)
                            .status("ACTIVE")
                            .createdBy(actor)
                            .updatedBy(actor)
                            .build());
                }
            }
        } else if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            toSave.add(ProductImage.builder()
                    .product(product)
                    .imageUrl(thumbnailUrl.trim())
                    .isPrimary(1)
                    .displayOrder(1)
                    .status("ACTIVE")
                    .createdBy(actor)
                    .updatedBy(actor)
                    .build());
        }
        if (!toSave.isEmpty()) {
            productImageRepository.saveAll(toSave);
        }
    }
}

