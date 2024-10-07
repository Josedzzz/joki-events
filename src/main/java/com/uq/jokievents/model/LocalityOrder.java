package com.uq.jokievents.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Id @JsonIgnore
    private String id;

    private String eventId;
    private String payingOrderId;
    private int numTicketsSelected;
    private String localityName;
    private Double totalPaymentAmount;
}
