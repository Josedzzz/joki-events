package com.uq.jokievents.dtos;

// This is not using @Valid annotation as it is just a holder of information, not an input.
public record LocalityOrderAsClientDTO(

        String eventId,

        String localityName,

        double totalPaymentAmount,

        int selectedTickets
) {}

