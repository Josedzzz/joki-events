package com.uq.jokievents.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uq.jokievents.model.enums.CouponType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "coupons")
@RequiredArgsConstructor
public class Coupon {
    @Id
    private String id;
    private String name;
    private double discountPercent;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") private LocalDateTime expirationDate;
    private double minPurchaseAmount;
    private CouponType couponType; //    UNIQUE or INDIVIDUAL
}
