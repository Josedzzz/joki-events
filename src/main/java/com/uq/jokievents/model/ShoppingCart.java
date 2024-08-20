package com.uq.jokievents.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "shoppingcarts")
public class ShoppingCart {

    @Id
    private String id;
    private ArrayList<ObjectId> idTickets;
    private Double totalPrice;
    private ObjectId idClient;

    //Contructors
    public ShoppingCart() {

    }

    public ShoppingCart(String id, ArrayList<ObjectId> idTickets, Double totalPrice, ObjectId idClient) {
        this.id = id;
        this.idTickets = idTickets;
        this.totalPrice = totalPrice;
        this.idClient = idClient;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<ObjectId> getIdTickets() {
        return idTickets;
    }

    public void setIdTickets(ArrayList<ObjectId> idTickets) {
        this.idTickets = idTickets;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public ObjectId getIdClient() {
        return idClient;
    }

    public void setIdClient(ObjectId idClient) {
        this.idClient = idClient;
    }
}
