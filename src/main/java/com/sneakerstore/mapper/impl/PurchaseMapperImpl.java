package com.sneakerstore.mapper.impl;

import com.sneakerstore.mapper.PurchaseMapper;

import com.sneakerstore.dto.response.PurchaseOrderDetailDto;
import com.sneakerstore.dto.response.PurchaseOrderDto;
import com.sneakerstore.entity.PurchaseOrder;
import com.sneakerstore.entity.PurchaseOrderDetail;
import com.sneakerstore.entity.ProductVariant;
import org.springframework.stereotype.Component;

@Component
public class PurchaseMapperImpl implements PurchaseMapper {

    @Override
    public PurchaseOrderDto toSummaryDto(PurchaseOrder po) {
        if (po == null) {
            return null;
        }
        return PurchaseOrderDto.builder()
                .id(po.getId())
                .purchaseCode(po.getPurchaseCode())
                .supplierId(po.getSupplier() != null ? po.getSupplier().getId() : null)
                .supplierCode(po.getSupplier() != null ? po.getSupplier().getSupplierCode() : null)
                .supplierName(po.getSupplier() != null ? po.getSupplier().getSupplierName() : null)
                .employeeId(po.getEmployee() != null ? po.getEmployee().getId() : null)
                .employeeName(po.getEmployee() != null ? po.getEmployee().getFullName() : null)
                .purchaseDate(po.getPurchaseDate())
                .totalAmount(po.getTotalAmount())
                .status(po.getStatus())
                .note(po.getNote())
                .confirmedAt(po.getConfirmedAt())
                .cancelledAt(po.getCancelledAt())
                .createdAt(po.getCreatedAt())
                .build();
    }

    @Override
    public PurchaseOrderDetailDto toDetailDto(PurchaseOrderDetail detail) {
        if (detail == null) {
            return null;
        }
        ProductVariant pv = detail.getProductVariant();
        return PurchaseOrderDetailDto.builder()
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
}

