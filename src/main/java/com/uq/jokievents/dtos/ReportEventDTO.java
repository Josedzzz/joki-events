package com.uq.jokievents.dtos;

import com.uq.jokievents.model.enums.EventType;

import java.time.LocalDateTime;

public record ReportEventDTO(
        String id,
        String name,
        String city,
        String eventDate,
        int totalAvailablePlaces,
        int remainingPlaces,
        int currentOccupancy,
        EventType eventType,
        double percentageSold
) {}

