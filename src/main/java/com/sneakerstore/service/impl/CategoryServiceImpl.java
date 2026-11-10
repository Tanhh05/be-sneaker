package com.sneakerstore.service.impl;

import com.sneakerstore.dto.request.CategoryRequest;
import com.sneakerstore.dto.response.CategoryDto;
import com.sneakerstore.entity.Category;
import com.sneakerstore.mapper.CategoryMapper;
import com.sneakerstore.repository.CategoryRepository;
import com.sneakerstore.service.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryDto> searchCategories(String keyword, String status, Pageable pageable) {
        Page<CategoryDto> page = categoryRepository.searchCategories(keyword, status, pageable)
                .map(categoryMapper::toDto);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAllByOrderByCategoryNameAsc().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại danh mục)
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        // Bước 5: Output Mapping
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto create(CategoryRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra trùng mã và tên danh mục)
        String code = request.getCategoryCode().trim().toUpperCase();
        String name = request.getCategoryName().trim();
        if (categoryRepository.existsByCategoryCodeIgnoreCase(code)) {
            throw new BusinessException("Mã danh mục đã tồn tại: " + code);
        }
        if (categoryRepository.existsByCategoryNameIgnoreCase(name)) {
            throw new BusinessException("Tên danh mục đã tồn tại: " + name);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Khởi tạo danh mục)
        String actor = SecurityUtils.getCurrentUsername();
        Category category = Category.builder()
                .categoryCode(code)
                .categoryName(name)
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                // Bước 4: Ghi vết / Audit
                .createdBy(actor)
                .updatedBy(actor)
                .build();

        // Bước 5: Output Mapping
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại bản ghi và trùng mã/tên danh mục)
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        String code = request.getCategoryCode().trim().toUpperCase();
        String name = request.getCategoryName().trim();

        if (!category.getCategoryCode().equalsIgnoreCase(code) && categoryRepository.existsByCategoryCodeIgnoreCase(code)) {
            throw new BusinessException("Mã danh mục đã tồn tại: " + code);
        }
        if (!category.getCategoryName().equalsIgnoreCase(name) && categoryRepository.existsByCategoryNameIgnoreCase(name)) {
            throw new BusinessException("Tên danh mục đã tồn tại: " + name);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Cập nhật thông tin)
        category.setCategoryCode(code);
        category.setCategoryName(name);
        category.setDescription(request.getDescription());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            category.setStatus(request.getStatus());
        }

        // Bước 4: Ghi vết / Audit
        category.setUpdatedBy(SecurityUtils.getCurrentUsername());

        // Bước 5: Output Mapping
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại và ràng buộc khóa ngoại sản phẩm)
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new BusinessException("Không thể xóa danh mục đã có " + productCount + " sản phẩm liên quan. Vui lòng chuyển trạng thái sang INACTIVE.");
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi xóa bản ghi
        categoryRepository.delete(category);
    }
}

