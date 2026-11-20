package com.sneakerstore.repository;

import com.sneakerstore.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, String status);

    boolean existsBySalesOrderIdAndProductId(Long salesOrderId, Long productId);

    List<ProductReview> findBySalesOrderId(Long salesOrderId);
}
