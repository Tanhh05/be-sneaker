package com.sneakerstore.mapper.impl;

import com.sneakerstore.mapper.EmployeeMapper;

import com.sneakerstore.dto.response.EmployeeDto;
import com.sneakerstore.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Override
    public EmployeeDto toDto(Employee employee) {
        if (employee == null) {
            return null;
        }
        return EmployeeDto.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .dateOfBirth(employee.getDateOfBirth())
                .gender(employee.getGender())
                .phone(employee.getPhone())
                .email(employee.getEmail())
                .address(employee.getAddress())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .userId(employee.getUser() != null ? employee.getUser().getId() : null)
                .username(employee.getUser() != null ? employee.getUser().getUsername() : null)
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}

