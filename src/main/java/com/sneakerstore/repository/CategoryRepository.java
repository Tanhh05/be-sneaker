package com.sneakerstore.repository;

import com.sneakerstore.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCategoryCodeIgnoreCase(String categoryCode);

    boolean existsByCategoryNameIgnoreCase(String categoryName);

    @Query("SELECT c FROM Category c WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(c.categoryCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR :status = '' OR c.status = :status)")
    Page<Category> searchCategories(@Param("keyword") String keyword,
                                    @Param("status") String status,
                                    Pageable pageable);

    List<Category> findAllByOrderByCategoryNameAsc();

    @Query(value = "SELECT COUNT(*) FROM PRODUCTS WHERE CATEGORY_ID = :categoryId", nativeQuery = true)
    long countProductsByCategoryId(@Param("categoryId") Long categoryId);
}

