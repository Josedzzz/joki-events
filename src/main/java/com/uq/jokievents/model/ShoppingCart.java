package com.uq.jokievents.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Data
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

}
