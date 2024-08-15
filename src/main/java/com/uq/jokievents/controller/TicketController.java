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
     * Gets a list of all tickets
     *
     * @return a ResponseEntity containing the list of ticket objects and an HTTP status of ok
     */
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketService.findAll();
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    /**
     * Gets a ticket by its id
     *
     * @param id the identifier of the ticket
     * @return a ResponseEntity containig the ticket object and HTTP status of ok if found, otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable String id) {
        Optional<Ticket> ticket = ticketService.findById(id);
        return ticket.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new ticket
     *
     * @param ticket the ticket object to be created
     * @return a ResponseEntity containig the created ticket and an HTTP status of created
     */
    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        Ticket newTicket = ticketService.save(ticket);
        return new ResponseEntity<>(newTicket, HttpStatus.CREATED);
    }

    /**
     * Updates an existing ticket
     *
     * @param id the identifier of the ticket
     * @param ticket the ticket object containing the updated data
     * @return a ResponseEntity containing the update ticket and an HTTP status of ok, otherwise not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable String id, @RequestBody Ticket ticket) {
        Optional<Ticket> existingTicket = ticketService.findById(id);
        if (existingTicket.isPresent()) {
            ticket.setId(id);
            Ticket updatedTicket = ticketService.save(ticket);
            return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes a ticket by its id
     *
     * @param id the identifier of the ticket object to be deleted
     * @return a ResponseEntity with and HTTP status of ok if the deletion is succesful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTicket(@PathVariable String id) {
        ticketService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
