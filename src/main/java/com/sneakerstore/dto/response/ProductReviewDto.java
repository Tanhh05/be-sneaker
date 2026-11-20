package com.sneakerstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewDto {
    private Long id;
    private Long productId;
    private String productName;
    private String customerName;
    private Integer rating;
    private String fitFeedback;
    private String comment;
    private List<String> photoUrls;
    private Boolean isPurchased;
    private LocalDateTime createdAt;
}
