package com.uq.jokievents.service;

import com.uq.jokievents.model.TicketOrder;
import com.uq.jokievents.repository.TicketOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketOrderService {

    @Autowired
    private TicketOrderRepository ticketOrderRepository;

    /**
     * Get a list of all ticketorders from the db
     *
     * @return a list of all ticketorder objects in the db
     */
    public List<TicketOrder> findAll() {
        return ticketOrderRepository.findAll();
    }

    /**
     * Gets a ticketorder by its id from the db
     *
     * @param id the identifier of the ticketorder
     * @return an Optional containing the ticketorder if found, empty optional if not
     */
    public Optional<TicketOrder> findById(String id) {
        return ticketOrderRepository.findById(id);
    }

    /**
     * Saves a new ticketorder or updates an existing in the db
     *
     * @param ticketOrder the ticketorder object to be saved or updated
     * @return the saved or updated ticketoderder object
     */
    public TicketOrder save(TicketOrder ticketOrder) {
        return ticketOrderRepository.save(ticketOrder);
    }

    /**
     * Deletes a ticketorder from the db using its id
     *
     * @param id the identifier of the ticketorder to be deleted
     */
    public void deleteById(String id) {
        ticketOrderRepository.deleteById(id);
    }
}
