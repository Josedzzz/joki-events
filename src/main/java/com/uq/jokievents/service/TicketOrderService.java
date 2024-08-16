package com.uq.jokievents.service;

import com.uq.jokievents.model.TicketOrder;
import com.uq.jokievents.repository.TicketOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketOrderService {

    @Autowired
    private TicketOrderRepository ticketOrderRepository;

    /**
     * Get a list of all ticketOrder from the db
     *
     * @return a ResponseEntity containing a list of ticketOrder objects and an HTTP status
     */
    public ResponseEntity<?> findAll() {
        try {
            List<TicketOrder> ticketOrders = ticketOrderRepository.findAll();
            return new ResponseEntity<>(ticketOrders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed ticketOrder request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a ticketOrder by its id
     *
     * @param id the identifier of the ticketOrder object
     * @return a ResponseEntity containing the ticketOrder and an HTTP status
     */
    public ResponseEntity<?> findById(String id) {
        try {
            Optional<TicketOrder> ticketOrder = ticketOrderRepository.findById(id);
            if (ticketOrder.isPresent()) {
                return new ResponseEntity<>(ticketOrder.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Ticket order not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed ticketOrder request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a new ticketOrder
     *
     * @param ticketOrder the ticketORder object to be created
     * @return a ResponseEntity containing the created ticketOrder and an HTTP status
     */
    public ResponseEntity<?> create(TicketOrder ticketOrder) {
        try {
            TicketOrder savedTicketOrder = ticketOrderRepository.save(ticketOrder);
            return new ResponseEntity<>(savedTicketOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to created ticketOrder", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update and existing ticketOrder by its id
     *
     * @param id the identifier of the ticketOrder object
     * @param ticketOrder the ticketOrder to be updated
     * @return a ResponseEntity containing the update ticketOrder and an HTTP status
     */
    public ResponseEntity<?> update(String id, TicketOrder ticketOrder) {
        try {
            Optional<TicketOrder> existingTicketOrder = ticketOrderRepository.findById(id);
            if (existingTicketOrder.isPresent()) {
                ticketOrder.setId(id);
                TicketOrder updatedTicketOrder = ticketOrderRepository.save(ticketOrder);
                return new ResponseEntity<>(updatedTicketOrder, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Ticket order not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to updated ticketOrder", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a ticketOrder by its id
     *
     * @param id the identifier of the ticketOrder object
     * @return a ResponseEntity with and HTTP status
     */
    public ResponseEntity<?> delete(String id) {
        try {
            Optional<TicketOrder> existingTicketOrder = ticketOrderRepository.findById(id);
            if (existingTicketOrder.isPresent()) {
                ticketOrderRepository.deleteById(id);
                return new ResponseEntity<>("Ticket order deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Ticket order not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to deleted ticketOrder", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
