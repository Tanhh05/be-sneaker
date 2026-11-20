package com.sneakerstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewSummaryDto {
    private Double averageRating;
    private Integer totalReviews;
    private Map<Integer, Long> starCounts;
    private Map<String, Long> fitCounts;
    private List<ProductReviewDto> reviews;
}
