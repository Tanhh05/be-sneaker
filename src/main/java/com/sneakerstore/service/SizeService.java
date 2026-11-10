package com.sneakerstore.service;

import com.sneakerstore.dto.request.SizeRequest;
import com.sneakerstore.dto.response.SizeDto;

import java.util.List;

public interface SizeService {

    List<SizeDto> getAllSizes();

    SizeDto createSize(SizeRequest request);

    SizeDto updateSize(Long id, SizeRequest request);
}
