package com.uq.jokievents.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "shoppingcarts")
public class ShoppingCart {

    @Id
    private String id;
    private ArrayList<String> idTickets;
    private Double totalPrice;
    private String idClient;

    //Contructors
    public ShoppingCart() {

    }

    public ShoppingCart(String id, ArrayList<String> idTickets, Double totalPrice, String idClient) {
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

    public ArrayList<String> getIdTickets() {
        return idTickets;
    }

    public void setIdTickets(ArrayList<String> idTickets) {
        this.idTickets = idTickets;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }
}
