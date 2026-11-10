package com.sneakerstore.repository;

import com.sneakerstore.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    boolean existsByOrderCodeIgnoreCase(String orderCode);

    @EntityGraph(attributePaths = {"customer", "employee"})
    Optional<SalesOrder> findWithHeaderById(Long id);

    @EntityGraph(attributePaths = {"customer", "employee"})
    List<SalesOrder> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    @EntityGraph(attributePaths = {"customer", "employee"})
    @Query("SELECT so FROM SalesOrder so WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(so.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR (so.customer IS NOT NULL AND LOWER(so.customer.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "OR (so.customer IS NOT NULL AND LOWER(so.customer.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
           "AND (:status IS NULL OR :status = '' OR so.status = :status) " +
           "AND (:fromDate IS NULL OR so.orderDate >= :fromDate) " +
           "AND (:toDate IS NULL OR so.orderDate <= :toDate)")
    Page<SalesOrder> searchOrders(@Param("keyword") String keyword,
                                  @Param("status") String status,
                                  @Param("fromDate") LocalDateTime fromDate,
                                  @Param("toDate") LocalDateTime toDate,
                                  Pageable pageable);

    long countByStatusIn(List<String> statuses);

    @Query("SELECT COALESCE(SUM(so.finalAmount), 0) FROM SalesOrder so WHERE so.status = 'COMPLETED' " +
           "AND (:fromDate IS NULL OR so.orderDate >= :fromDate) " +
           "AND (:toDate IS NULL OR so.orderDate <= :toDate)")
    BigDecimal sumCompletedRevenue(@Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate") LocalDateTime toDate);
}

