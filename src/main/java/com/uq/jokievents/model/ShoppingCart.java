package com.uq.jokievents.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Data
@Document(collection = "shopping-carts")
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCart {

    @Id
    private String id;

    private String paymentGatewayId; // Will be assigned when a payment is made
    private String idClient;
    private ArrayList<LocalityOrder> localityOrders;
    // Two prices to show in the frontend a kind of "discount" thing and make think the client he is spending less.
    private Double totalPrice; // Price of all the localities a client may have
    private Double totalPriceWithDiscount; // Price of all the localities a client may have with discount.
    private OrderPayment orderPayment; // Will be assigned when payment time comes!
    private String paymentCoupon;
    private Double appliedDiscountPercent;
    private boolean couponClaimed;
    // TODO Add private LocalDateTime purchaseDate.
}
