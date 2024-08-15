package com.uq.jokievents.controller;

import com.uq.jokievents.model.Event;
import com.uq.jokievents.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * Gets a list of all events
     *
     * @return a ResponseEntity containing a list of event object and an HTTP status of ok
     */
    @GetMapping()
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.findAll();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    /**
     * Gets an event by its id
     *
     * @param id the identifier of the event object
     * @return a ResponseEntity containing the event object and HTTP status of ok if found, otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable String id) {
        Optional<Event> event = eventService.findById(id);
        return event.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new event
     *
     * @param event the event object to be created
     * @return a ResponseEntity containing the created event object and an HTTP status of created
     */
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event newEvent = eventService.save(event);
        return new ResponseEntity<>(newEvent, HttpStatus.CREATED);
    }

    /**
     * Updates an existing event
     *
     * @param id the identifier of the event to be updated
     * @param event the event object containing the updated data
     * @return a ResponseEntity containing the updated event and an HTTP status of ok, otherwise not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable String id, @RequestBody Event event) {
        Optional<Event> existingEvent = eventService.findById(id);
        if (existingEvent.isPresent()) {
            event.setId(id);
            Event updatedEvent = eventService.save(event);
            return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes an event by its id
     *
     * @param id the identifier of the event object
     * @return a ResponseEntity with an HTTP status of ok if the deletion is correct
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Event> deleteEvent(@PathVariable String id) {
        eventService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
