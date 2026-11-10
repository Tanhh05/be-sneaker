package com.sneakerstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    private String categoryCode;
    private String categoryName;
    private String description;
    private String status;
    private long productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

