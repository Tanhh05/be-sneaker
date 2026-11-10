package com.sneakerstore.service;

import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.PurchaseOrderRequest;
import com.sneakerstore.dto.response.PurchaseOrderDto;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PurchaseService {

    PageResponse<PurchaseOrderDto> searchPurchases(
            String keyword, Long supplierId, String status, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    PurchaseOrderDto getById(Long id);

    PurchaseOrderDto create(PurchaseOrderRequest request);

    PurchaseOrderDto update(Long id, PurchaseOrderRequest request);

    PurchaseOrderDto confirm(Long id);

    PurchaseOrderDto cancel(Long id);
}
