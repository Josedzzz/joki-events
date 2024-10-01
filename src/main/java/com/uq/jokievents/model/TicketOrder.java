package com.uq.jokievents.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "ticket-orders")
@AllArgsConstructor
@NoArgsConstructor
// TODO Think if this will be added to a ShoppingCart as a reference of time. If not, delete it and merge it along ShoppingCart itself.
public class TicketOrder {
    @Id
    private String id;
    private LocalDateTime purchaseDate;
}
