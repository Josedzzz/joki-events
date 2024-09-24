package com.uq.jokievents.service.implementation;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.repository.EventRepository;
import com.uq.jokievents.service.interfaces.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class    EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    /**
     * Get a list of all events from the db
     *
     * @return a list of all event objects in the db
     */
    public ResponseEntity<?> findAll() {
        try {
            List<Event> events = eventRepository.findAll();
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed event request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets an event by its id
     *
     * @param id the identifier of the event
     * @return an Optional containing the event if found, empty if not
     */
    public ResponseEntity<?> findById(String id) {
        try {
            Optional<Event> event = eventRepository.findById(id);
            if (event.isPresent()) {
                return new ResponseEntity<>(event.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed event request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Saves a new event or updates an existing in the db
     *
     * @param event the event object to be saver or updated
     * @return the saved or updated event object
     */
    public ResponseEntity<?> create(Event event) {
        try {
            Event createdEvent = eventRepository.save(event);
            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing client by id
     *
     * @param id the identifierof the event to be update
     * @param event the updated event object
     * @return a ResponseEntity containing the updated event object and an HTTP status
     */
    public ResponseEntity<?> update(String id, Event event) {
        try {
            Optional<Event> existingEvent = eventRepository.findById(id);
            if (existingEvent.isPresent()) {
                event.setId(id);
                Event updatedEvent = eventRepository.save(event);
                return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a event from the db using its id
     *
     * @param id the identifier of the event object
     */
    public ResponseEntity<?> deleteById(String id) {
        try {
            Optional<Event> existingEvent = eventRepository.findById(id);
            if (existingEvent.isPresent()) {
                eventRepository.deleteById(id);
                return new ResponseEntity<>("Event deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

