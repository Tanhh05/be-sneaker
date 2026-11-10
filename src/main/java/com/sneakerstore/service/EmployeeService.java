package com.sneakerstore.service;

import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.EmployeeRequest;
import com.sneakerstore.dto.response.EmployeeDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    PageResponse<EmployeeDto> searchEmployees(String keyword, String status, Pageable pageable);

    List<EmployeeDto> getAllEmployees();

    EmployeeDto getById(Long id);

    EmployeeDto create(EmployeeRequest request);

    EmployeeDto update(Long id, EmployeeRequest request);

    EmployeeDto toggleStatus(Long id, String status);
}
