package com.uq.jokievents.dtos;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record HandleEventDTO(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Address is required")
        String address,

        @Future(message = "Date must be in the future")
        LocalDateTime date,

        @Min(value = 1, message = "Total available places must be at least 1")
        int totalAvailablePlaces,

        @NotBlank(message = "URL is required")
        String eventImageURL,

        @NotEmpty(message = "At least one locality is required")
        List<@Valid CreateLocalityDTO> localities
) {}
