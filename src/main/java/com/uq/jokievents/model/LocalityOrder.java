package com.uq.jokievents.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "tickets")
@AllArgsConstructor
@NoArgsConstructor
public class LocalityOrder {

    @Id
    private String id;
    private ObjectId idClient;
    private int numTicketsSelected;
    private String localityName;
    private Double totalPaymentAmount;
}
