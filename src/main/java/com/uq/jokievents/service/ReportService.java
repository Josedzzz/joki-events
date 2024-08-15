package com.uq.jokievents.service;

import com.uq.jokievents.model.Report;
import com.uq.jokievents.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
     * @return a list of all reportsobjects in the db
     */
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    /**
     * Gets a report by its id from the db
     *
     * @param id the identifier of the report
     * @return an Optional containing the report if found, empty Optional if not
     */
    public Optional<Report> findById(String id) {
        return reportRepository.findById(id);
    }

    /**
     * Saves a new report or updates an existing in the db
     *
     * @param report the report object to be saves or updated
     * @return the saved or updated report object
     */
    public Report save(Report report) {
        return reportRepository.save(report);
    }

    /**
     * Deletes a report from the db using its id
     *
     * @param id the identifier of the report to be deleted
     */
    public void deleteById(String id) {
        reportRepository.deleteById(id);
    }

}
