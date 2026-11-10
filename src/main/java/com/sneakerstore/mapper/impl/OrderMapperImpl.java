package com.sneakerstore.mapper.impl;

import com.sneakerstore.mapper.OrderMapper;

import com.sneakerstore.dto.response.OrderStatusHistoryDto;
import com.sneakerstore.dto.response.SalesOrderDetailDto;
import com.sneakerstore.dto.response.SalesOrderDto;
import com.sneakerstore.entity.OrderStatusHistory;
import com.sneakerstore.entity.SalesOrder;
import com.sneakerstore.entity.SalesOrderDetail;
import com.sneakerstore.dto.response.PaymentDto;
import com.sneakerstore.entity.Payment;
import com.sneakerstore.entity.ProductVariant;
import org.springframework.stereotype.Component;

@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public SalesOrderDto toSummaryDto(SalesOrder so) {
        if (so == null) {
            return null;
        }
        return SalesOrderDto.builder()
                .id(so.getId())
                .orderCode(so.getOrderCode())
                .customerId(so.getCustomer() != null ? so.getCustomer().getId() : null)
                .customerCode(so.getCustomer() != null ? so.getCustomer().getCustomerCode() : null)
                .customerName(so.getCustomer() != null ? so.getCustomer().getFullName() : "Khách lẻ tại quầy")
                .customerPhone(so.getCustomer() != null ? so.getCustomer().getPhone() : "---")
                .employeeId(so.getEmployee() != null ? so.getEmployee().getId() : null)
                .employeeName(so.getEmployee() != null ? so.getEmployee().getFullName() : so.getCreatedBy())
                .orderDate(so.getOrderDate())
                .totalAmount(so.getTotalAmount())
                .finalAmount(so.getFinalAmount())
                .paymentMethod(so.getPaymentMethod())
                .status(so.getStatus())
                .note(so.getNote())
                .createdAt(so.getCreatedAt())
                .build();
    }

    @Override
    public SalesOrderDetailDto toDetailDto(SalesOrderDetail detail) {
        if (detail == null) {
            return null;
        }
        ProductVariant pv = detail.getProductVariant();
        return SalesOrderDetailDto.builder()
                .id(detail.getId())
                .productVariantId(pv != null ? pv.getId() : null)
                .productId(pv != null && pv.getProduct() != null ? pv.getProduct().getId() : null)
                .productName(pv != null && pv.getProduct() != null ? pv.getProduct().getProductName() : null)
                .sku(pv != null ? pv.getSku() : null)
                .sizeValue(pv != null && pv.getSize() != null ? pv.getSize().getSizeValue() : null)
                .colorName(pv != null && pv.getColor() != null ? pv.getColor().getColorName() : null)
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .subtotal(detail.getSubtotal())
                .build();
    }

    @Override
    public PaymentDto toPaymentDto(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentDto.builder()
                .id(payment.getId())
                .paymentCode(payment.getPaymentCode())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .referenceNote(payment.getReferenceNote())
                .build();
    }

    @Override
    public OrderStatusHistoryDto toHistoryDto(OrderStatusHistory history) {
        if (history == null) {
            return null;
        }
        return OrderStatusHistoryDto.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .note(history.getNote())
                .changedAt(history.getChangedAt())
                .createdBy(history.getCreatedBy())
                .build();
    }
}

