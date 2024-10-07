package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.ReportEventDTO;
import com.uq.jokievents.service.interfaces.CSVExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CSVExportServiceImpl implements CSVExportService {


    @Override

    public void exportEventsReportToCSV(List<ReportEventDTO> events, PrintWriter writer) {
        writer.write("ID,Name,City,Event Date,Total Available Places,Remaining Places,Current Occupancy,Event Type,Percentage Sold\n");

        for (ReportEventDTO event : events) {
            writer.write(String.format(
                    "%s,%s,%s,%s,%d,%d,%d,%s,%.2f\n",
                    event.id(),
                    event.name(),
                    event.city(),
                    event.eventDate().toString(),
                    event.totalAvailablePlaces(),
                    event.remainingPlaces(),
                    event.currentOccupancy(),
                    event.eventType(),
                    event.percentageSold()
            ));
        }
    }
}
