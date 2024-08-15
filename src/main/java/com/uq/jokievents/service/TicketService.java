package com.uq.jokievents.service;

import com.uq.jokievents.model.Ticket;
import com.uq.jokievents.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Get a list of all tickets from the db
     *
     * @return a list of all tickets objects in the db
     */
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    /**
     * Gets a ticket by its id from the db
     *
     * @param id identifier of the ticker
     * @return an Optional object containing the ticket if found
     */
    public Optional<Ticket> findById(String id) {
        return ticketRepository.findById(id);
    }

    /**
     * Saves a new ticker or updates an existing in the db
     *
     * @param ticket the ticker object to be saved or updated
     * @return the saver or updated ticker object
     */
    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * Deletes a ticket from the db using its id
     *
     * @param id the identifier of the ticket
     */
    public void deleteById(String id) {
        ticketRepository.deleteById(id);
    }

}
