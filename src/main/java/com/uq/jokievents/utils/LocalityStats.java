package com.uq.jokievents.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalityStats {
    private String localityName;
    private int ticketsSold;
    private int totalTickets;
    private double soldPercentage;
    private BigDecimal localityRevenue;
}
