package com.sneakerstore.repository;

import com.sneakerstore.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsByProductIdAndSizeIdAndColorId(Long productId, Long sizeId, Long colorId);

    @EntityGraph(attributePaths = {"product", "product.brand", "product.category", "size", "color"})
    Optional<ProductVariant> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"product", "product.brand", "product.category", "size", "color"})
    List<ProductVariant> findByIdIn(Collection<Long> ids);

    @EntityGraph(attributePaths = {"product", "product.brand", "product.category", "size", "color"})
    List<ProductVariant> findByProductIdOrderByIdAsc(Long productId);

    @EntityGraph(attributePaths = {"product", "product.brand", "product.category", "size", "color"})
    @Query("SELECT pv FROM ProductVariant pv WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(pv.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(pv.product.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(pv.product.productCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:productId IS NULL OR pv.product.id = :productId) " +
           "AND (:sizeId IS NULL OR pv.size.id = :sizeId) " +
           "AND (:colorId IS NULL OR pv.color.id = :colorId) " +
           "AND (:status IS NULL OR :status = '' OR pv.status = :status)")
    Page<ProductVariant> searchVariants(@Param("keyword") String keyword,
                                        @Param("productId") Long productId,
                                        @Param("sizeId") Long sizeId,
                                        @Param("colorId") Long colorId,
                                        @Param("status") String status,
                                        Pageable pageable);

    @EntityGraph(attributePaths = {"product", "product.brand", "product.category", "size", "color"})
    List<ProductVariant> findAllByOrderByIdAsc();
}

