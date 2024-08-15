package com.uq.jokievents.service;

import com.uq.jokievents.model.Event;
import com.uq.jokievents.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    /**
     * Get a list of all events from the db
     *
     * @return a list of all event objects in the db
     */
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    /**
     * Gets an event by its id
     *
     * @param id the identifier of the event
     * @return an Optional containing the event if found, empty if not
     */
    public Optional<Event> findById(String id) {
        return eventRepository.findById(id);
    }

    /**
     * Saves a new event or updates an existing in the db
     *
     * @param event the event object to be saver or updated
     * @return the saved or updated event object
     */
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    /**
     * Deletes a event from the db using its id
     *
     * @param id the identifier of the event object
     */
    public void deleteById(String id) {
        eventRepository.deleteById(id);
    }

}
