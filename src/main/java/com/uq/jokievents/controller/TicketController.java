package com.uq.jokievents.controller;

import com.uq.jokievents.model.Ticket;
import com.uq.jokievents.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    /**
     * Get all tickets
     *
     * @return a ResponseEntity object with containing tickets
     */
    @GetMapping
    public ResponseEntity<?> getAllTickets() {
        return ticketService.findAll();
    }

    /**
     * Get a ticket by its id
     *
     * @param id the identifier of the ticket object to find
     * @return a ResponseEntity containig the ticket
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable String id) {
        return ticketService.findById(id);
    }

    /**
     * Create a new ticket
     *
     * @param ticket the ticket object to be created
     * @return a ResponseEntity containing the created ticket
     */
    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket) {
        return ticketService.create(ticket);
    }

    /**
     * Update an existing ticket by id
     *
     * @param id the identifier of the ticket object to update
     * @param ticket the update ticket object
     * @return a ResponseEntity containing the update ticket
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable String id, @RequestBody Ticket ticket) {
        return ticketService.update(id, ticket);
    }

    /**
     * Delete ticket by id
     *
     * @param id the identifier of the ticket to delete
     * @return a ResponseEntity object with an HTTP status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable String id) {
        return ticketService.delete(id);
    }

}
