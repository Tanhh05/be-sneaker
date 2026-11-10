package com.sneakerstore.controller;

import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.response.CustomerDto;
import com.sneakerstore.dto.request.CustomerRequest;
import com.sneakerstore.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ApiResponse<PageResponse<CustomerDto>> getCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(customerService.searchCustomers(
                keyword, status, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))));
    }

    @GetMapping("/all")
    public ApiResponse<List<CustomerDto>> getAllCustomers() {
        return ApiResponse.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(customerService.getById(id));
    }

    @PostMapping
    public ApiResponse<CustomerDto> create(@Valid @RequestBody CustomerRequest request) {
        return ApiResponse.ok("Thêm khách hàng thành công", customerService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerDto> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return ApiResponse.ok("Cập nhật khách hàng thành công", customerService.update(id, request));
    }
}

