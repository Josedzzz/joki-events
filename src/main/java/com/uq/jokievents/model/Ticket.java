package com.uq.jokievents.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "tickets")
public class Ticket {

    @Id
    private String id;
    private ObjectId idClient;
    private ObjectId idCoupon;
    private int numTickets;
    private String locality;
    private Double netPaymentAmount;
    private Double finalPaymentAmount;

    // Contructor
    public Ticket() {}

    public Ticket(String id, ObjectId idClient, ObjectId idCoupon, int numTickets, String locality, Double netPaymentAmount, Double finalPaymentAmount) {
        this.id = id;
        this.idClient = idClient;
        this.idCoupon = idCoupon;
        this.numTickets = numTickets;
        this.locality = locality;
        this.netPaymentAmount = netPaymentAmount;
        this.finalPaymentAmount = finalPaymentAmount;
    }

}
