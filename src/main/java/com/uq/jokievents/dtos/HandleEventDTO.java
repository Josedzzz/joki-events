package com.uq.jokievents.dtos;

import com.uq.jokievents.model.enums.EventType;

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

        @NotEmpty(message = "At least one locality is required")
        List<CreateLocalityDTO> localities,

        @NotNull
        String eventImageUrl,

        @NotNull
        String localitiesImageUrl,

        @NotNull(message = "Event type is required")
        EventType eventType
) {}

