package com.uq.jokievents.controller;

import com.uq.jokievents.model.Event;
import com.uq.jokievents.service.interfaces.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> getAllEvents() {
        return eventService.findAll();
    }

    /**
     * Gets an event by its id
     *
     * @param id the identifier of the event object
     * @return a ResponseEntity containing the event object and HTTP status of ok if found, otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable String id) {
        return eventService.findById(id);
    }

    /**
     * Creates a new event
     *
     * @param event the event object to be created
     * @return a ResponseEntity containing the created event object and an HTTP status of created
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        return eventService.create(event);
    }

    /**
     * Updates an existing event
     *
     * @param id the identifier of the event to be updated
     * @param event the event object containing the updated data
     * @return a ResponseEntity containing the updated event and an HTTP status of ok, otherwise not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable String id, @RequestBody Event event) {
        return eventService.update(id, event);
    }

    /**
     * Deletes an event by its id
     *
     * @param id the identifier of the event object
     * @return a ResponseEntity with an HTTP status of ok if the deletion is correct
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id) {
        return eventService.deleteById(id);
    }

}
