package com.uq.jokievents.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Data
@Document(collection = "shopping-carts")
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCart {

    @Id
    private String id;
    private String idClient;
    private ArrayList<LocalityOrder> localityOrders;
    private Double totalPrice; // Price of all the localities a client may have
}
