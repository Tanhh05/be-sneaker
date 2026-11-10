package com.sneakerstore.repository;

import com.sneakerstore.entity.SalesOrderDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderDetailRepository extends JpaRepository<SalesOrderDetail, Long> {

    @EntityGraph(attributePaths = {
            "productVariant",
            "productVariant.product",
            "productVariant.size",
            "productVariant.color"
    })
    List<SalesOrderDetail> findBySalesOrderIdOrderByIdAsc(Long salesOrderId);
}

