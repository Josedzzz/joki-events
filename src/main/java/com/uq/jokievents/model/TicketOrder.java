package com.uq.jokievents.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Document(collection = "ticketorders")
public class TicketOrder {

    @Id
    private String id;
    private ObjectId idTicket;
    private LocalDateTime purchaseDate;

    // Constructor
    public TicketOrder() {

    }

    public TicketOrder(String id, ObjectId idTicket, LocalDateTime purchaseDate) {
        this.id = id;
        this.idTicket = idTicket;
        this.purchaseDate = purchaseDate;
    }

}
