package com.uq.jokievents.service.implementation;

import com.uq.jokievents.model.LocalityOrder;
import com.uq.jokievents.repository.TicketRepository;
import com.uq.jokievents.service.interfaces.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Get a list of all tickets
     *
     * @return a ResponseEnteity containing a list of ticket objects and an HTTP status
     */
    public ResponseEntity<?> findAll() {
        try {
            List<LocalityOrder> localityOrders = ticketRepository.findAll();
            return new ResponseEntity<>(localityOrders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed tickets request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a ticket by its id
     *
     * @param id the identifier of the ticket object
     * @return a ResponseEntity containing the ticket and an HTTP status
     */
    public ResponseEntity<?> findById(String id) {
        try {
            Optional<LocalityOrder> ticket = ticketRepository.findById(id);
            if (ticket.isPresent()) {
                return new ResponseEntity<>(ticket.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Ticket not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed tickets request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a new ticket
     *
     * @param localityOrder the ticket object to be created
     * @return a ResponseEntity containing the created ticket with an HTTP status
     */
    public ResponseEntity<?> create(LocalityOrder localityOrder) {
        try {
            LocalityOrder createdLocalityOrder = ticketRepository.save(localityOrder);
            return new ResponseEntity<>(createdLocalityOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create a ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing ticket
     *
     * @param id the identifier of the ticket to be updated
     * @param localityOrder the updated ticket object
     * @return a ResponseEntity containing the update ticket and an HTTP status
     */
    public ResponseEntity<?> update(String id, LocalityOrder localityOrder) {
        try {
            Optional<LocalityOrder> existingTicket = ticketRepository.findById(id);
            if (existingTicket.isPresent()) {
                localityOrder.setId(id);
                LocalityOrder updatedLocalityOrder = ticketRepository.save(localityOrder);
                return new ResponseEntity<>(updatedLocalityOrder, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Ticket not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a ticket by its id
     *
     * @param id the identifier of the ticket object to be deleted
     * @return a ResponseEntity with an HTTP status
     */
    public ResponseEntity<?> delete(String id) {
        try {
            Optional<LocalityOrder> existingTicket = ticketRepository.findById(id);
            if (existingTicket.isPresent()) {
                ticketRepository.deleteById(id);
                return new ResponseEntity<>("Ticket deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Ticket not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
