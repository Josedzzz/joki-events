package com.uq.jokievents.model;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "clients")
public class Client {

    @Id
    private String id;
    private String idCard;
    private String name;
    private String direction;
    private String phoneNumber;
    private String email;
    private String password;
    private ArrayList<ObjectId> idCoupons;
    private ObjectId idShoppingCart;
    private boolean active;

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
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<ObjectId> getIdCoupons() {
        return idCoupons;
    }

    public void setIdCoupons(ArrayList<ObjectId> idCoupons) {
        this.idCoupons = idCoupons;
    }

    public ObjectId getIdShoppingCart() {
        return idShoppingCart;
    }

    public void setIdShoppingCart(ObjectId idShoppingCart) {
        this.idShoppingCart = idShoppingCart;
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
}
