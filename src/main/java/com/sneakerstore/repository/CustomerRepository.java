package com.sneakerstore.repository;

import com.sneakerstore.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByCustomerCodeIgnoreCase(String customerCode);

    boolean existsByPhone(String phone);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR :status = '' OR c.status = :status)")
    Page<Customer> searchCustomers(@Param("keyword") String keyword,
                                   @Param("status") String status,
                                   Pageable pageable);

    List<Customer> findAllByOrderByIdDesc();
}

