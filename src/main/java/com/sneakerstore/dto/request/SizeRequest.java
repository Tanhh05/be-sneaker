package com.sneakerstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SizeRequest {
    @NotBlank(message = "Giá trị Size không được để trống")
    private String sizeValue;

    @NotNull(message = "Số Size không được để trống")
    private BigDecimal sizeNumber;

    private String description;
    private String status = "ACTIVE";
}

