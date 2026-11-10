package com.sneakerstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ColorRequest {
    @NotBlank(message = "Mã màu không được để trống")
    private String colorCode;

    @NotBlank(message = "Tên màu không được để trống")
    private String colorName;

    @NotBlank(message = "Mã HEX không được để trống")
    private String hexCode;

    private String status = "ACTIVE";
}

