package com.uq.jokievents.controller;

import com.uq.jokievents.model.TicketOrder;
import com.uq.jokievents.service.TicketOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ticketorders")
public class TicketOrderController {

    @Autowired
    private TicketOrderService ticketOrderService;

    /**
     * Get all ticketOrders
     *
     * @return a ResponseEntity object eith containing ticketOrders
     */
    @GetMapping
    public ResponseEntity<?> getAllTicketOrders() {
        return ticketOrderService.findAll();
    }

    /**
     * Get a ticketOrder by id
     *
     * @param id the identifier of the ticketOrder to find
     * @return a ResponseEntity containing the ticketOrder
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketOrderById(@PathVariable String id) {
        return ticketOrderService.findById(id);
    }

    /**
     * Create a new ticketOrder
     *
     * @param ticketOrder the ticketOrder object to be created
     * @return a ResponseEntity containing the created ticketOrder
     */
    @PostMapping
    public ResponseEntity<?> createTicketOrder(@RequestBody TicketOrder ticketOrder) {
        return ticketOrderService.create(ticketOrder);
    }

    /**
     * Update an existing ticketOrder by id
     *
     * @param id the identifier of the ticketOrder object
     * @param ticketOrder the updated ticketOrder object
     * @return a ResponseEntity containing the update ticketOrder
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicketOrder(@PathVariable String id, @RequestBody TicketOrder ticketOrder) {
        return ticketOrderService.update(id, ticketOrder);
    }

    /**
     * Delete a ticketOrder by id
     *
     * @param id the identifier of the ticketOrder to be deleted
     * @return a ResponseEntity object with an HTTP status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicketOrder(@PathVariable String id) {
        return ticketOrderService.delete(id);
    }

}
