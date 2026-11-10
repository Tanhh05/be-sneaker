package com.sneakerstore.repository;

import com.sneakerstore.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    boolean existsByPurchaseCodeIgnoreCase(String purchaseCode);

    @EntityGraph(attributePaths = {"supplier", "employee"})
    Optional<PurchaseOrder> findWithHeaderById(Long id);

    @EntityGraph(attributePaths = {"supplier", "employee"})
    @Query("SELECT po FROM PurchaseOrder po WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(po.purchaseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(po.supplier.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:supplierId IS NULL OR po.supplier.id = :supplierId) " +
           "AND (:status IS NULL OR :status = '' OR po.status = :status) " +
           "AND (:fromDate IS NULL OR po.purchaseDate >= :fromDate) " +
           "AND (:toDate IS NULL OR po.purchaseDate <= :toDate)")
    Page<PurchaseOrder> searchPurchaseOrders(@Param("keyword") String keyword,
                                             @Param("supplierId") Long supplierId,
                                             @Param("status") String status,
                                             @Param("fromDate") LocalDateTime fromDate,
                                             @Param("toDate") LocalDateTime toDate,
                                             Pageable pageable);
}

