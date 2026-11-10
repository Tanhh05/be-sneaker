package com.sneakerstore.service.impl;

import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.common.util.SecurityUtils;
import com.sneakerstore.dto.request.EmployeeRequest;
import com.sneakerstore.dto.response.EmployeeDto;
import com.sneakerstore.entity.Employee;
import com.sneakerstore.mapper.EmployeeMapper;
import com.sneakerstore.repository.EmployeeRepository;
import com.sneakerstore.service.EmployeeService;
import com.sneakerstore.entity.User;
import com.sneakerstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeDto> searchEmployees(String keyword, String status, Pageable pageable) {
        Page<EmployeeDto> page = employeeRepository.searchEmployees(keyword, status, pageable)
                .map(employeeMapper::toDto);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAllByOrderByIdAsc().stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getById(Long id) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại nhân viên)
        Employee employee = employeeRepository.findWithUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));
        // Bước 5: Output Mapping
        return employeeMapper.toDto(employee);
    }

    @Override
    @Transactional
    public EmployeeDto create(EmployeeRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra trùng mã NV, SĐT, email và liên kết tài khoản User)
        String code = request.getEmployeeCode().trim().toUpperCase();
        String phone = request.getPhone().trim();
        if (employeeRepository.existsByEmployeeCodeIgnoreCase(code)) {
            throw new BusinessException("Mã nhân viên đã tồn tại: " + code);
        }
        if (employeeRepository.existsByPhone(phone)) {
            throw new BusinessException("Số điện thoại nhân viên đã tồn tại: " + phone);
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && employeeRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new BusinessException("Email nhân viên đã tồn tại: " + request.getEmail());
        }

        User linkedUser = null;
        if (request.getUserId() != null) {
            if (employeeRepository.existsByUserId(request.getUserId())) {
                throw new BusinessException("Tài khoản này đã được liên kết với nhân viên khác");
            }
            linkedUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + request.getUserId()));
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Khởi tạo hồ sơ nhân sự)
        String actor = SecurityUtils.getCurrentUsername();
        Employee employee = Employee.builder()
                .employeeCode(code)
                .fullName(request.getFullName().trim())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(phone)
                .email(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null)
                .address(request.getAddress())
                .position(request.getPosition().trim())
                .hireDate(request.getHireDate())
                .user(linkedUser)
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                // Bước 4: Ghi vết / Audit
                .createdBy(actor)
                .updatedBy(actor)
                .build();

        // Bước 5: Output Mapping
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeDto update(Long id, EmployeeRequest request) {
        // Bước 1: Validation nghiệp vụ (Kiểm tra tồn tại hồ sơ, trùng lặp mã, SĐT, email và User liên kết)
        Employee employee = employeeRepository.findWithUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));

        String code = request.getEmployeeCode().trim().toUpperCase();
        String phone = request.getPhone().trim();

        if (!employee.getEmployeeCode().equalsIgnoreCase(code)
                && employeeRepository.existsByEmployeeCodeIgnoreCase(code)) {
            throw new BusinessException("Mã nhân viên đã tồn tại: " + code);
        }
        if (!employee.getPhone().equals(phone) && employeeRepository.existsByPhone(phone)) {
            throw new BusinessException("Số điện thoại nhân viên đã tồn tại: " + phone);
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().trim().equalsIgnoreCase(employee.getEmail())
                && employeeRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new BusinessException("Email nhân viên đã tồn tại: " + request.getEmail());
        }

        if (request.getUserId() != null) {
            boolean differentUser = employee.getUser() == null || !employee.getUser().getId().equals(request.getUserId());
            if (differentUser && employeeRepository.existsByUserId(request.getUserId())) {
                throw new BusinessException("Tài khoản này đã được liên kết với nhân viên khác");
            }
            User linkedUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + request.getUserId()));
            employee.setUser(linkedUser);
        } else {
            employee.setUser(null);
        }

        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi (Cập nhật hồ sơ nhân viên)
        employee.setEmployeeCode(code);
        employee.setFullName(request.getFullName().trim());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setPhone(phone);
        employee.setEmail(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null);
        employee.setAddress(request.getAddress());
        employee.setPosition(request.getPosition().trim());
        employee.setHireDate(request.getHireDate());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            employee.setStatus(request.getStatus());
        }

        // Bước 4: Ghi vết / Audit
        employee.setUpdatedBy(SecurityUtils.getCurrentUsername());

        // Bước 5: Output Mapping
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeDto toggleStatus(Long id, String status) {
        // Bước 1: Validation nghiệp vụ
        Employee employee = employeeRepository.findWithUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));
        // Bước 2 & 3: Quản lý Transaction & Thực thi logic lõi
        employee.setStatus(status);
        // Bước 4: Ghi vết / Audit
        employee.setUpdatedBy(SecurityUtils.getCurrentUsername());
        // Bước 5: Output Mapping
        return employeeMapper.toDto(employeeRepository.save(employee));
    }
}

