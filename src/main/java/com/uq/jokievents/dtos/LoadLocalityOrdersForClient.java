package com.uq.jokievents.dtos;

import com.uq.jokievents.model.enums.EventType;

import java.time.LocalDateTime;

public record LoadLocalityOrdersForClient(

        String payingOrderId,
        int numTicketsSelected,
        String localityName,
        Double totalPaymentAmount,

        String eventName,
        String address,
        String city,
        LocalDateTime eventDate,
        String eventImageUrl,
        EventType eventType
) {
}
