package com.sneakerstore.service;

import com.sneakerstore.dto.request.ColorRequest;
import com.sneakerstore.dto.response.ColorDto;

import java.util.List;

public interface ColorService {

    List<ColorDto> getAllColors();

    ColorDto createColor(ColorRequest request);

    ColorDto updateColor(Long id, ColorRequest request);
}
