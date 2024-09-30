package com.uq.jokievents.dtos;

// This is not using @Valid annotation as it is just a holder of information, not an input. This makes me think if all dtos need validation?
public record LocalityOrderAsClientDTO(

        String eventId,

        String localityName,

        double totalPaymentAmount,

        int ticketsSelected
) {}

