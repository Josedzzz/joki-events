package com.uq.jokievents.dtos;

import com.uq.jokievents.model.enums.EventType;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

// DTO for search parameters, no validation because I did the validation on the Service implementation hahahahahaahahhaahha
public record SearchEventDTO(
        String eventName,
        String city,
        LocalDateTime startDate,
        LocalDateTime endDate,
        EventType eventType
) {}
