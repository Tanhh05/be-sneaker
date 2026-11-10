package com.sneakerstore.repository;

import com.sneakerstore.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByPaymentCodeIgnoreCase(String paymentCode);
    List<Payment> findBySalesOrderIdOrderByIdAsc(Long salesOrderId);
}

