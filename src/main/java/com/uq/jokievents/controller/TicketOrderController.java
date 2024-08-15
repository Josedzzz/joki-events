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
     * Get a list of all ticketorders
     *
     * @return a ResponseEntity containing a list of ticketorders objects and an HTTP status of ok
     */
    @GetMapping
    public ResponseEntity<List<TicketOrder>> getAllTicketOrders() {
        List<TicketOrder> ticketOrders = ticketOrderService.findAll();
        return new ResponseEntity<>(ticketOrders, HttpStatus.OK);
    }

    /**
     * Gets a ricket order by its id
     *
     * @param id the identifier of the ticketorder object
     * @return a ResponseEntity containing the ticketorder object and an HTTP status of ok if found, otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketOrder> getTicketOrderById(@PathVariable String id) {
        Optional<TicketOrder> ticketOrder = ticketOrderService.findById(id);
        return ticketOrder.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new ticketorder
     *
     * @param ticketOrder the ticketorder object to be created
     * @return a ResponseEntity containing the created ticketorder object and an HTTP status of created
     */
    @PostMapping
    public ResponseEntity<TicketOrder> createTicketOrder(@RequestBody TicketOrder ticketOrder) {
        TicketOrder newTicketOrder = ticketOrderService.save(ticketOrder);
        return new ResponseEntity<>(newTicketOrder, HttpStatus.CREATED);
    }

    /**
     * Updates an existing ticketorder
     *
     * @param id the identifier of the ticketorder to be updated
     * @param ticketOrder the ticketorder object containing the update data
     * @return a ResponseEntity containing the update ticketorder object and an HTTP status of ok, otherwise not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<TicketOrder> updateTicketOrder(@PathVariable String id, @RequestBody TicketOrder ticketOrder) {
        Optional<TicketOrder> existingTicketOrder = ticketOrderService.findById(id);
        if (existingTicketOrder.isPresent()) {
            ticketOrder.setId(id);
            TicketOrder updatedTicketOrder = ticketOrderService.save(ticketOrder);
            return new ResponseEntity<>(updatedTicketOrder, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes a ticketorder by its id
     *
     * @param id the identifier of the ticketorder object
     * @return a ResponseEntity object with an HTTP status of ok if the deletion is succesull
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<TicketOrder> deleteTicketOrder(@PathVariable String id) {
        ticketOrderService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
