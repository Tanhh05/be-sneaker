package com.sneakerstore.repository;

import com.sneakerstore.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByBrandCodeIgnoreCase(String brandCode);

    boolean existsByBrandNameIgnoreCase(String brandName);

    @Query("SELECT b FROM Brand b WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(b.brandCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.brandName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR :status = '' OR b.status = :status)")
    Page<Brand> searchBrands(@Param("keyword") String keyword,
                             @Param("status") String status,
                             Pageable pageable);

    List<Brand> findAllByOrderByBrandNameAsc();
}

