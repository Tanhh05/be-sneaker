package com.sneakerstore.mapper.impl;

import com.sneakerstore.mapper.SupplierMapper;

import com.sneakerstore.dto.response.SupplierDto;
import com.sneakerstore.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapperImpl implements SupplierMapper {

    @Override
    public SupplierDto toDto(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        return SupplierDto.builder()
                .id(supplier.getId())
                .supplierCode(supplier.getSupplierCode())
                .supplierName(supplier.getSupplierName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .taxCode(supplier.getTaxCode())
                .status(supplier.getStatus())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}

