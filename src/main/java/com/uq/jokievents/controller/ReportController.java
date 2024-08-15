package com.uq.jokievents.controller;

import com.uq.jokievents.model.Report;
import com.uq.jokievents.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<List<Report>> getAllReports() {
        List<Report> reports = reportService.findAll();
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }

    /**
     * Gets a report by its id
     *
     * @param id the identifier of the report
     * @return a ResponseEntity containing the report object and an HTTP status of ok if found, otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable String id) {
        Optional<Report> report = reportService.findById(id);
        return report.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new report
     *
     * @param report the report object to be creates
     * @return a ResponseEntity containing the created report object and an HTTP status of created
     */
    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody Report report) {
        Report savedReport = reportService.save(report);
        return new ResponseEntity<>(savedReport, HttpStatus.CREATED);
    }

    /**
     * Updates an existing report
     *
     * @param id the identifier of the report to be updated
     * @param report the report object containing the update data
     * @return a ResponseEntity containing the update report object and an HTTP status of ok, otherwise not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Report> updateReport(@PathVariable String id, @RequestBody Report report) {
        Optional<Report> existingReport = reportService.findById(id);
        if (existingReport.isPresent()) {
            report.setId(id);
            Report updatedReport = reportService.save(report);
            return new ResponseEntity<>(updatedReport, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes a report by its id
     *
     * @param id the identifier of the report to be deleted
     * @return a ResponseEntity with an HTTP status of ok if the deletion is correct
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Report> deleteReport(@PathVariable String id) {
        reportService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
