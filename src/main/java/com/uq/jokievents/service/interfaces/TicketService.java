package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.model.Ticket;
import org.springframework.http.ResponseEntity;

public interface TicketService {

    ResponseEntity<?> findAll();
    ResponseEntity<?> findById(String id);
    ResponseEntity<?> create(Ticket ticket);
    ResponseEntity<?> update(String id, Ticket ticket);
    ResponseEntity<?> delete(String id);

}
