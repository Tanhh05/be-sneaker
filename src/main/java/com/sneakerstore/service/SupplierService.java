package com.sneakerstore.service;

import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.SupplierRequest;
import com.sneakerstore.dto.response.SupplierDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupplierService {

    PageResponse<SupplierDto> searchSuppliers(String keyword, String status, Pageable pageable);

    List<SupplierDto> getAllSuppliers();

    SupplierDto getById(Long id);

    SupplierDto create(SupplierRequest request);

    SupplierDto update(Long id, SupplierRequest request);

    SupplierDto deactivate(Long id);
}
