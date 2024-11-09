package com.uq.jokievents.dtos;

import jakarta.validation.constraints.*;

public record CreateLocalityDTO(

        @NotBlank(message = "Name is required")
        String localityName,

        @Positive(message = "Price must be positive")
        double price,

        @Min(value = 1, message = "Max capacity must be at least 1")
        int maxCapacity
) {}
