package com.uq.jokievents.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

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

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Integer> getSellDistributionLocalityes() {
        return sellDistributionLocalityes;
    }

    public void setSellDistributionLocalityes(Map<String, Integer> sellDistributionLocalityes) {
        this.sellDistributionLocalityes = sellDistributionLocalityes;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }
}
