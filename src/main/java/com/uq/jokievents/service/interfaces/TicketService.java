package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.model.LocalityOrder;
import org.springframework.http.ResponseEntity;

public interface TicketService {

    ResponseEntity<?> findAll();
    ResponseEntity<?> findById(String id);
    ResponseEntity<?> create(LocalityOrder localityOrder);
    ResponseEntity<?> update(String id, LocalityOrder localityOrder);
    ResponseEntity<?> delete(String id);

}
