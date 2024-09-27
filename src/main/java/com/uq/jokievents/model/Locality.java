package com.uq.jokievents.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "distributionlocalities")
public class Locality {

    @Id
    private String id;  // Mongo
    private String name;
    private double price;
    private int maxCapacity;
    private int currentOccupancy = 0;

}
