package com.sneakerstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeDto {
    private Long id;
    private String sizeValue;
    private BigDecimal sizeNumber;
    private String description;
    private String status;
}

