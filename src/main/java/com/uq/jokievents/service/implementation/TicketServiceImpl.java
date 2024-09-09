package com.uq.jokievents.service.implementation;

import com.uq.jokievents.model.Ticket;
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
            List<Ticket> tickets = ticketRepository.findAll();
            return new ResponseEntity<>(tickets, HttpStatus.OK);
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
            Optional<Ticket> ticket = ticketRepository.findById(id);
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
     * @param ticket the ticket object to be created
     * @return a ResponseEntity containing the created ticket with an HTTP status
     */
    public ResponseEntity<?> create(Ticket ticket) {
        try {
            Ticket createdTicket = ticketRepository.save(ticket);
            return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create a ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing ticket
     *
     * @param id the identifier of the ticket to be updated
     * @param ticket the updated ticket object
     * @return a ResponseEntity containing the update ticket and an HTTP status
     */
    public ResponseEntity<?> update(String id, Ticket ticket) {
        try {
            Optional<Ticket> existingTicket = ticketRepository.findById(id);
            if (existingTicket.isPresent()) {
                ticket.setId(id);
                Ticket updatedTicket = ticketRepository.save(ticket);
                return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
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
            Optional<Ticket> existingTicket = ticketRepository.findById(id);
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
