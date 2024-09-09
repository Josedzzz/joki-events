package com.uq.jokievents.controller;

import com.uq.jokievents.model.Report;
import com.uq.jokievents.service.interfaces.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Gets a list of all reports
     *
     * @return a ResponseEntity containing a list of Report objects and an HTTP status of ok
     */
    @GetMapping
    public ResponseEntity<?> getAllReports() {
        return reportService.findAll();
    }

    /**
     * Gets a report by its id
     *
     * @param id the identifier of the report
     * @return a ResponseEntity containing the report object and an HTTP status of ok if found, otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable String id) {
        return reportService.findById(id);
    }

    /**
     * Creates a new report
     *
     * @param report the report object to be creates
     * @return a ResponseEntity containing the created report object and an HTTP status of created
     */
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody Report report) {
        return reportService.create(report);
    }

    /**
     * Updates an existing report
     *
     * @param id the identifier of the report to be updated
     * @param report the report object containing the update data
     * @return a ResponseEntity containing the update report object and an HTTP status of ok, otherwise not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReport(@PathVariable String id, @RequestBody Report report) {
        return reportService.update(id, report);
    }

    /**
     * Deletes a report by its id
     *
     * @param id the identifier of the report to be deleted
     * @return a ResponseEntity with an HTTP status of ok if the deletion is correct
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable String id) {
        return reportService.deleteById(id);
    }

}
