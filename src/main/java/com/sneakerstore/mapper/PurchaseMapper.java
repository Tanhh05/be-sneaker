package com.sneakerstore.mapper;

import com.sneakerstore.dto.response.PurchaseOrderDetailDto;
import com.sneakerstore.dto.response.PurchaseOrderDto;
import com.sneakerstore.entity.PurchaseOrder;
import com.sneakerstore.entity.PurchaseOrderDetail;

public interface PurchaseMapper {
    PurchaseOrderDto toSummaryDto(PurchaseOrder po);
    PurchaseOrderDetailDto toDetailDto(PurchaseOrderDetail detail);
}
