package com.uq.jokievents.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "purchases")
public class Purchase {

    @Id private String id;
    private String clientId;  // Reference to the Client's ID.
    private LocalDateTime purchaseDate;
    private List<LocalityOrder> purchasedItems;
    private BigDecimal totalAmount;
    private String paymentMethod;
}

