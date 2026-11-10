package com.sneakerstore.service.impl;

import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.common.util.SecurityUtils;
import com.sneakerstore.dto.request.SupplierRequest;
import com.sneakerstore.dto.response.SupplierDto;
import com.sneakerstore.entity.Supplier;
import com.sneakerstore.mapper.SupplierMapper;
import com.sneakerstore.repository.SupplierRepository;
import com.sneakerstore.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierDto> searchSuppliers(String keyword, String status, Pageable pageable) {
        Page<SupplierDto> page = supplierRepository.searchSuppliers(keyword, status, pageable)
                .map(supplierMapper::toDto);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierDto> getAllSuppliers() {
        return supplierRepository.findAllByOrderBySupplierNameAsc().stream()
                .map(supplierMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDto getById(Long id) {
        // Bước 1: Validation nghiệp vụ
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + id));
        // Bước 5: Output Mapping
        return supplierMapper.toDto(supplier);
    }

    @Override
    @Transactional
    public SupplierDto create(SupplierRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra trùng mã nhà cung cấp)
        String code = request.getSupplierCode().trim().toUpperCase();
        if (supplierRepository.existsBySupplierCodeIgnoreCase(code)) {
            throw new BusinessException("Mã nhà cung cấp đã tồn tại: " + code);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Khởi tạo nhà cung cấp)
        String actor = SecurityUtils.getCurrentUsername();
        Supplier supplier = Supplier.builder()
                .supplierCode(code)
                .supplierName(request.getSupplierName().trim())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone().trim())
                .email(request.getEmail())
                .address(request.getAddress())
                .taxCode(request.getTaxCode())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                // Bước 4: Ghi vết / Audit
                .createdBy(actor)
                .updatedBy(actor)
                .build();

        // Bước 5: Output Mapping
        return supplierMapper.toDto(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierDto update(Long id, SupplierRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại bản ghi và trùng mã NCC)
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + id));
        String code = request.getSupplierCode().trim().toUpperCase();
        if (!supplier.getSupplierCode().equalsIgnoreCase(code) && supplierRepository.existsBySupplierCodeIgnoreCase(code)) {
            throw new BusinessException("Mã nhà cung cấp đã tồn tại: " + code);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Cập nhật thông tin đối tác)
        supplier.setSupplierCode(code);
        supplier.setSupplierName(request.getSupplierName().trim());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone().trim());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setTaxCode(request.getTaxCode());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            supplier.setStatus(request.getStatus());
        }

        // Bước 4: Ghi vết / Audit
        supplier.setUpdatedBy(SecurityUtils.getCurrentUsername());

        // Bước 5: Output Mapping
        return supplierMapper.toDto(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierDto deactivate(Long id) {
        // Bước 1: Validation nghiệp vụ
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + id));
        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Ngừng hợp tác)
        supplier.setStatus("INACTIVE");
        // Bước 4: Ghi vết / Audit
        supplier.setUpdatedBy(SecurityUtils.getCurrentUsername());
        // Bước 5: Output Mapping
        return supplierMapper.toDto(supplierRepository.save(supplier));
    }
}

