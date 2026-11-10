package com.sneakerstore.service.impl;

import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.common.util.SecurityUtils;
import com.sneakerstore.dto.request.CustomerRequest;
import com.sneakerstore.dto.response.CustomerDto;
import com.sneakerstore.entity.Customer;
import com.sneakerstore.mapper.CustomerMapper;
import com.sneakerstore.repository.CustomerRepository;
import com.sneakerstore.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerDto> searchCustomers(String keyword, String status, Pageable pageable) {
        Page<CustomerDto> page = customerRepository.searchCustomers(keyword, status, pageable)
                .map(customerMapper::toDto);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAllByOrderByIdDesc().stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getById(Long id) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại khách hàng)
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + id));
        // Bước 5: Output Mapping
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional
    public CustomerDto create(CustomerRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra trùng lặp số điện thoại và mã khách hàng)
        String phone = request.getPhone().trim();
        if (customerRepository.existsByPhone(phone)) {
            throw new BusinessException("Số điện thoại khách hàng đã tồn tại: " + phone);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Sinh mã tự động nếu để trống & khởi tạo hồ sơ)
        String code = request.getCustomerCode();
        if (code == null || code.isBlank()) {
            long count = customerRepository.count() + 1;
            code = String.format("KH%03d", count);
            while (customerRepository.existsByCustomerCodeIgnoreCase(code)) {
                count++;
                code = String.format("KH%03d", count);
            }
        } else {
            code = code.trim().toUpperCase();
            if (customerRepository.existsByCustomerCodeIgnoreCase(code)) {
                throw new BusinessException("Mã khách hàng đã tồn tại: " + code);
            }
        }

        String actor = SecurityUtils.getCurrentUsername();
        Customer customer = Customer.builder()
                .customerCode(code)
                .fullName(request.getFullName().trim())
                .phone(phone)
                .email(request.getEmail())
                .gender(request.getGender() != null ? request.getGender() : "MALE")
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                // Bước 4: Ghi vết / Audit
                .createdBy(actor)
                .updatedBy(actor)
                .build();

        // Bước 5: Output Mapping
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerDto update(Long id, CustomerRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại bản ghi, trùng lặp SĐT và mã KH)
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + id));

        String phone = request.getPhone().trim();
        if (!customer.getPhone().equals(phone) && customerRepository.existsByPhone(phone)) {
            throw new BusinessException("Số điện thoại khách hàng đã tồn tại: " + phone);
        }

        if (request.getCustomerCode() != null && !request.getCustomerCode().isBlank()) {
            String code = request.getCustomerCode().trim().toUpperCase();
            if (!customer.getCustomerCode().equalsIgnoreCase(code)
                    && customerRepository.existsByCustomerCodeIgnoreCase(code)) {
                throw new BusinessException("Mã khách hàng đã tồn tại: " + code);
            }
            customer.setCustomerCode(code);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Cập nhật thông tin hồ sơ)
        customer.setFullName(request.getFullName().trim());
        customer.setPhone(phone);
        customer.setEmail(request.getEmail());
        customer.setGender(request.getGender());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            customer.setStatus(request.getStatus());
        }

        // Bước 4: Ghi vết / Audit
        customer.setUpdatedBy(SecurityUtils.getCurrentUsername());

        // Bước 5: Output Mapping
        return customerMapper.toDto(customerRepository.save(customer));
    }
}

