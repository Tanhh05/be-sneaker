package com.sneakerstore.mapper;

import com.sneakerstore.dto.response.SupplierDto;
import com.sneakerstore.entity.Supplier;

public interface SupplierMapper {
    SupplierDto toDto(Supplier supplier);
}
