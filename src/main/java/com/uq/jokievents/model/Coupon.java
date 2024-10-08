package com.uq.jokievents.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "coupons")
public class Coupon {

    @Id
    private String id;
    private String name;
    private double discountPercent;
    private LocalDateTime expirationDate;
    private double minPurchaseAmount;
    private boolean isUsed = false; // Change to amount of times used or active

    // Constructors
    public Coupon() {

    }

    public Coupon(String id, double discountPercent, LocalDateTime expirationDate, double minPurchaseAmount) {
        this.id = id;
        this.discountPercent = discountPercent;
        this.expirationDate = expirationDate;
        this.minPurchaseAmount = minPurchaseAmount;
    }

}
