package com.uq.jokievents.model;

import java.util.ArrayList;
import java.time.LocalDateTime;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "clients")
public class Client {

    @Id
    private String id; // Mongo will take care of it.
    private String idCard; // Kind of a country given identifier.
    private String name;
    private String direction; // Deberías ser "address" pero me da miedo cambiarlo.
    private String phoneNumber;
    private String email;
    private String password;
    private ArrayList<ObjectId> idCoupons; // TODO Implementar la clase CouponID (¿Cómo sé qué no eres uno de ellos?)
    private ObjectId idShoppingCart;
    private boolean active;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiration;

    // Constructors
    public Client() {
    }

    public Client(String idCard, String name, String direction, String phoneNumber, String email, String password,
            ArrayList<ObjectId> idCoupons, ObjectId shoppingCart) {
        this.idCard = idCard;
        this.name = name;
        this.direction = direction;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.idCoupons = idCoupons;
        this.idShoppingCart = shoppingCart;
        this.active = false;
        this.verificationCode = null;
        this.verificationCodeExpiration = null;
    }
}
