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

    @Id private String id;
    private String clientId;
    private String paymentGatewayId;
    private ArrayList<LocalityOrder> localityOrders;
    private Double totalPrice;
    private Double totalPriceWithDiscount;
    private Double appliedDiscountPercent;
    private boolean couponClaimed;
}
