package com.sneakerstore.repository;

import com.sneakerstore.entity.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Long> {

    @EntityGraph(attributePaths = {
            "productVariant",
            "productVariant.product",
            "productVariant.size",
            "productVariant.color"
    })
    List<PurchaseOrderDetail> findByPurchaseOrderIdOrderByIdAsc(Long purchaseOrderId);
}

