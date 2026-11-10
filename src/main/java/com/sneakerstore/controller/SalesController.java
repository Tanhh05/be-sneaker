package com.sneakerstore.controller;

import com.sneakerstore.common.constant.AppConstants;
import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.response.SalesOrderDto;
import com.sneakerstore.dto.request.SalesOrderRequest;
import com.sneakerstore.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<PageResponse<SalesOrderDto>> getSales(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(orderService.searchOrders(
                keyword, status, fromDate, toDate,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))));
    }

    @PostMapping
    public ApiResponse<SalesOrderDto> createSale(@Valid @RequestBody SalesOrderRequest request) {
        return ApiResponse.ok("Tạo đơn hàng thành công", orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<SalesOrderDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getById(id));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<SalesOrderDto> confirmSale(
            @PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String note = payload != null ? payload.get("note") : null;
        return ApiResponse.ok("Xác nhận đơn hàng thành công",
                orderService.updateStatus(id, AppConstants.ORDER_CONFIRMED, note));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<SalesOrderDto> completeSale(
            @PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String note = payload != null ? payload.get("note") : null;
        return ApiResponse.ok("Hoàn thành đơn hàng thành công",
                orderService.updateStatus(id, AppConstants.ORDER_COMPLETED, note));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SalesOrderDto> cancelSale(
            @PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String note = payload != null ? payload.get("note") : null;
        return ApiResponse.ok("Đã hủy đơn hàng",
                orderService.updateStatus(id, AppConstants.ORDER_CANCELLED, note));
    }
}

