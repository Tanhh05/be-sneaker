package com.sneakerstore.service;

import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.SalesOrderRequest;
import com.sneakerstore.dto.response.SalesOrderDto;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    PageResponse<SalesOrderDto> searchOrders(
            String keyword, String status, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    List<SalesOrderDto> getOrdersByCustomer(Long customerId);

    SalesOrderDto getById(Long id);

    SalesOrderDto createOrder(SalesOrderRequest request);

    SalesOrderDto updateStatus(Long id, String targetStatus, String note);
}
