package com.sneakerstore.mapper;

import com.sneakerstore.dto.response.OrderStatusHistoryDto;
import com.sneakerstore.dto.response.PaymentDto;
import com.sneakerstore.dto.response.SalesOrderDetailDto;
import com.sneakerstore.dto.response.SalesOrderDto;
import com.sneakerstore.entity.OrderStatusHistory;
import com.sneakerstore.entity.Payment;
import com.sneakerstore.entity.SalesOrder;
import com.sneakerstore.entity.SalesOrderDetail;

public interface OrderMapper {
    SalesOrderDto toSummaryDto(SalesOrder so);
    SalesOrderDetailDto toDetailDto(SalesOrderDetail detail);
    PaymentDto toPaymentDto(Payment payment);
    OrderStatusHistoryDto toHistoryDto(OrderStatusHistory history);
}
