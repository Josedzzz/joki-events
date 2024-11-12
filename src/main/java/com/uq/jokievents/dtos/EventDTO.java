package com.uq.jokievents.dtos;

import com.uq.jokievents.model.Locality;
import com.uq.jokievents.model.enums.EventType;

import java.util.List;

public record EventDTO(
        String id,
        String name,
        String address,
        String city,
        java.time.LocalDateTime eventDate,
        boolean availableForPurchase,
        List<Locality> localities,
        int totalAvailablePlaces,
        int finalTotalPlaces,
        String eventImageUrl,
        String localitiesImageUrl,
        EventType eventType
) {
}
