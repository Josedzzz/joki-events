package com.uq.jokievents.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = "reports")
public class Report {

    @Id
    private String id;
    private Map<String, Integer> sellDistributionLocalityes;
    private double profit;

    // Constructor
    public Report() {}

    public Report(String id, Map<String, Integer> sellDistributionLocalityes, double profit) {
        this.id = id;
        this.sellDistributionLocalityes = sellDistributionLocalityes;
        this.profit = profit;
    }

}
