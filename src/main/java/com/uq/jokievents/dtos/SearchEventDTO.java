package com.uq.jokievents.dtos;

import com.uq.jokievents.model.enums.EventType;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

// DTO for search parameters, no validation because it does not need it
public record SearchEventDTO(
        @NotBlank(message = "An event name must be specified")
        String eventName,

        String city,
        LocalDateTime startDate,
        LocalDateTime endDate,
        EventType eventType
) {}
