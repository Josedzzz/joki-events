package com.uq.jokievents.service;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.Report;
import com.uq.jokievents.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    /**
     * Get a list of all reports from the db
     *
     * @return a listof all report objects in the db
     */
    public ResponseEntity<?> findAll() {
        try {
            List<Report> reports = reportRepository.findAll();
            return new ResponseEntity<>(reports, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed reports request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets a report by its id from the db
     *
     * @param id the identifier of the report
     * @return an Optional containing the client if found, empty Optional if not
     */
    public ResponseEntity<?> findById(String id) {
        try {
            Optional<Report> report = reportRepository.findById(id);
            if (report.isPresent()) {
                return new ResponseEntity<>(report.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Report not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed report request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Saves a new report or updates an existing in the db
     * @param report
     * @return
     */
    public ResponseEntity<?> create(Report report) {
        try {
            Report createdReport = reportRepository.save(report);
            return new ResponseEntity<>(createdReport, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create report", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing client by id
     *
     * @param id the identifierof the report to be update
     * @param report the updated report object
     * @return a ResponseEntity containing the updated report objec and an HTTP status
     */
    public ResponseEntity<?> update(String id, Report report) {
        try {
            Optional<Report> existingReport = reportRepository.findById(id);
            if (existingReport.isPresent()) {
                report.setId(id);
                Report updatedReport = reportRepository.save(report);
                return new ResponseEntity<>(updatedReport, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Report not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update report", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a report from the db using its id
     *
     * @param id the identifier of the report to be deleted
     */
    public ResponseEntity<?> deleteById(String id) {
        try {
            Optional<Report> existingReport = reportRepository.findById(id);
            if (existingReport.isPresent()) {
                reportRepository.deleteById(id);
                return new ResponseEntity<>("Report deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Report not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete report", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
