package com.uq.jokievents.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ObjectId getIdClient() {
        return idClient;
    }

    public void setIdClient(ObjectId idClient) {
        this.idClient = idClient;
    }

    public ObjectId getIdCoupon() {
        return idCoupon;
    }

    public void setIdCoupon(ObjectId idCoupon) {
        this.idCoupon = idCoupon;
    }

    public int getNumTickets() {
        return numTickets;
    }

    public void setNumTickets(int numTickets) {
        this.numTickets = numTickets;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public Double getNetPaymentAmount() {
        return netPaymentAmount;
    }

    public void setNetPaymentAmount(Double netPaymentAmount) {
        this.netPaymentAmount = netPaymentAmount;
    }

    public Double getFinalPaymentAmount() {
        return finalPaymentAmount;
    }

    public void setFinalPaymentAmount(Double finalPaymentAmount) {
        this.finalPaymentAmount = finalPaymentAmount;
    }
}
