package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.model.TicketOrder;
import org.springframework.http.ResponseEntity;

public interface TicketOrderService {

    public ResponseEntity<?> findAll();
    public ResponseEntity<?> findById(String id);
    ResponseEntity<?> create(TicketOrder ticketOrder);
    ResponseEntity<?> update(String id, TicketOrder ticketOrder);
    ResponseEntity<?> delete(String id);

}
