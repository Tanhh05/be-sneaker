package com.sneakerstore.service;

import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.CustomerRequest;
import com.sneakerstore.dto.response.CustomerDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    PageResponse<CustomerDto> searchCustomers(String keyword, String status, Pageable pageable);

    List<CustomerDto> getAllCustomers();

    CustomerDto getById(Long id);

    CustomerDto create(CustomerRequest request);

    CustomerDto update(Long id, CustomerRequest request);
}
