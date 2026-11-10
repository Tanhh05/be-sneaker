package com.sneakerstore.repository;

import com.sneakerstore.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColorRepository extends JpaRepository<Color, Long> {
    boolean existsByColorCodeIgnoreCase(String colorCode);
    boolean existsByColorNameIgnoreCase(String colorName);
    List<Color> findAllByOrderByIdAsc();
}

