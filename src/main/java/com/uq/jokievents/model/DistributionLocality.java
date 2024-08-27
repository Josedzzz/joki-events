package com.uq.jokievents.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "distributionlocalities")
public class DistributionLocality {

    @Id
    private String id;
    private String name;
    private double price;
    private int maxCapacity;
    private String imapeDistributionLocality;

    // constructor
    public DistributionLocality() {}

    public DistributionLocality(String id, String name, double price, int maxCapacity, String imapeDistributionLocality) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.maxCapacity = maxCapacity;
        this.imapeDistributionLocality = imapeDistributionLocality;
    }

}
