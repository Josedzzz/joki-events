package com.uq.jokievents.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
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

}
