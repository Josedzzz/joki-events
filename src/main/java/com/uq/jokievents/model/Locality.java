package com.uq.jokievents.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "distributionlocalities")
public class Locality {

    @Id
    private String id;  // Mongo
    private String name;
    private double price;
    private int maxCapacity;
    private String imageDistributionLocality;
    private int currentOccupancy = 0;

    // constructor
    public Locality() {}

    public Locality(String name, double price, int maxCapacity, String imageDistributionLocality) {
        this.name = name;
        this.price = price;
        this.maxCapacity = maxCapacity;
        this.imageDistributionLocality = imageDistributionLocality;
    }

}
