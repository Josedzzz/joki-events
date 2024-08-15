package com.uq.jokievents.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "coupons")
public class Coupon {

    @Id
    private String id;
    private double discountPercent;
    private Date expirationDate;
    private double minPurchaseQuantity;
    private boolean isUsed;

    // Constructors
    public Coupon() {

    }

    public Coupon(String id, double discountPercent, Date expirationDate, double minPurchaseQuantity, boolean isUsed) {
        this.id = id;
        this.discountPercent = discountPercent;
        this.expirationDate = expirationDate;
        this.minPurchaseQuantity = minPurchaseQuantity;
        this.isUsed = isUsed;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public double getMinPurchaseQuantity() {
        return minPurchaseQuantity;
    }

    public void setMinPurchaseQuantity(double minPurchaseQuantity) {
        this.minPurchaseQuantity = minPurchaseQuantity;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }
}
