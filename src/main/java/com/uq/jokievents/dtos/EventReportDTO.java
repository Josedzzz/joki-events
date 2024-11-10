package com.uq.jokievents.dtos;

import com.uq.jokievents.utils.LocalityStats;

import java.math.BigDecimal;
import java.util.List;

public record EventReportDTO(
        String eventId,
        String eventName,
        String eventCity,
        BigDecimal totalRevenue,
        List<LocalityStats> localityStats
) {
}
