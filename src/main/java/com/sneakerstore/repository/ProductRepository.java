package com.sneakerstore.repository;

import com.sneakerstore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByProductCodeIgnoreCase(String productCode);

    @EntityGraph(attributePaths = {"brand", "category"})
    Optional<Product> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"brand", "category"})
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:status IS NULL OR :status = '' OR p.status = :status)")
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("brandId") Long brandId,
                                 @Param("categoryId") Long categoryId,
                                 @Param("status") String status,
                                 Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "category"})
    List<Product> findAllByOrderByIdDesc();

    long countByStatus(String status);
}

