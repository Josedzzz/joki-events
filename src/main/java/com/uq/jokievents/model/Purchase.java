package com.uq.jokievents.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data@AllArgsConstructor@NoArgsConstructor@Builder
@Document(collection = "purchases")
public class Purchase {

    @Id private String id;
    private String clientId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime purchaseDate;
    private List<LocalityOrder> purchasedItems;
    private BigDecimal totalAmount;
    private String paymentMethod;
}

