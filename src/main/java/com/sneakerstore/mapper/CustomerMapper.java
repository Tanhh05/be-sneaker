package com.sneakerstore.mapper;

import com.sneakerstore.dto.response.CustomerDto;
import com.sneakerstore.entity.Customer;

public interface CustomerMapper {
    CustomerDto toDto(Customer customer);
}
