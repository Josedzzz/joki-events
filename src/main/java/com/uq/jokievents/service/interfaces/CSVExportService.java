package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.ReportEventDTO;

import java.io.PrintWriter;
import java.util.List;

public interface CSVExportService {

    void exportEventsReportToCSV(List<ReportEventDTO> events, PrintWriter writer);
}

