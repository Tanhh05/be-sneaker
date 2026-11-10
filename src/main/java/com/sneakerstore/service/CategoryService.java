package com.sneakerstore.service;

import com.sneakerstore.dto.request.CategoryRequest;
import com.sneakerstore.dto.response.CategoryDto;
import com.sneakerstore.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    PageResponse<CategoryDto> searchCategories(String keyword, String status, Pageable pageable);

    List<CategoryDto> getAllCategories();

    CategoryDto getById(Long id);

    CategoryDto create(CategoryRequest request);

    CategoryDto update(Long id, CategoryRequest request);

    void delete(Long id);
}
