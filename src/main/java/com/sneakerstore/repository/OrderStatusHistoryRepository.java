package com.sneakerstore.repository;

import com.sneakerstore.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findBySalesOrderIdOrderByChangedAtAscIdAsc(Long salesOrderId);
}

