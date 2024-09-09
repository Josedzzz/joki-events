package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.model.Report;
import org.springframework.http.ResponseEntity;

public interface ReportService {

    ResponseEntity<?> findAll();
    ResponseEntity<?> findById(String id);
    ResponseEntity<?> create(Report report);
    ResponseEntity<?> update(String id, Report report);
    ResponseEntity<?> deleteById(String id);

}
