package com.sneakerstore.service;

import com.sneakerstore.dto.request.BrandRequest;
import com.sneakerstore.dto.response.BrandDto;
import com.sneakerstore.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BrandService {

    PageResponse<BrandDto> searchBrands(String keyword, String status, Pageable pageable);

    List<BrandDto> getAllBrands();

    BrandDto getById(Long id);

    BrandDto create(BrandRequest request);

    BrandDto update(Long id, BrandRequest request);

    BrandDto deactivate(Long id);
}
