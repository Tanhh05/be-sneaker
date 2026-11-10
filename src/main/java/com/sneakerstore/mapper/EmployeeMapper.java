package com.sneakerstore.mapper;

import com.sneakerstore.dto.response.EmployeeDto;
import com.sneakerstore.entity.Employee;

public interface EmployeeMapper {
    EmployeeDto toDto(Employee employee);
}
