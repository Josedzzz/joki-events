package com.uq.jokievents.dtos;

import javax.validation.constraints.*;

public record CreateLocalityDTO(

        @NotBlank(message = "Name is required")
        String name,

        @Positive(message = "Price must be positive")
        double price,

        @Min(value = 1, message = "Max capacity must be at least 1")
        int maxCapacity
) {}
