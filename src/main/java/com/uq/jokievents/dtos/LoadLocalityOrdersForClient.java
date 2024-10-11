package com.uq.jokievents.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uq.jokievents.model.enums.EventType;

import java.time.LocalDateTime;

public record LoadLocalityOrdersForClient(

        String payingOrderId,
        int numTicketsSelected,
        String localityName,
        Double totalPaymentAmount,

        String eventId,
        String eventName,
        String address,
        String city,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") LocalDateTime eventDate,
        String eventImageUrl,
        EventType eventType
) {
}
