package com.sneakerstore.repository;

import com.sneakerstore.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsBySupplierCodeIgnoreCase(String supplierCode);

    @Query("SELECT s FROM Supplier s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(s.supplierCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR :status = '' OR s.status = :status)")
    Page<Supplier> searchSuppliers(@Param("keyword") String keyword,
                                   @Param("status") String status,
                                   Pageable pageable);

    List<Supplier> findAllByOrderBySupplierNameAsc();
}

