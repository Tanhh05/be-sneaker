package com.sneakerstore.repository;

import com.sneakerstore.entity.ShoeSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SizeRepository extends JpaRepository<ShoeSize, Long> {
    boolean existsBySizeValueIgnoreCase(String sizeValue);
    List<ShoeSize> findAllByOrderBySizeNumberAsc();
}

