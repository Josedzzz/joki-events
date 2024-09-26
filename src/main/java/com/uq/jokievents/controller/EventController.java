package com.uq.jokievents.controller;

import com.uq.jokievents.model.Event;
import com.uq.jokievents.model.enums.EventType;
import com.uq.jokievents.service.interfaces.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// TODO Filter events by date,
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EventController {

    private final EventService eventService;

    /**
     * Gets an event by its id
     *
     * @param id the identifier of the event object
     * @return a ResponseEntity containing the event object and HTTP status of ok if found, otherwise the status is not found
     */
    @GetMapping("/get-event/{id}")
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
    @PostMapping("/update-event/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable String id, @RequestBody Event event) {
        return eventService.update(id, event);
    }

    /**
     * Deletes an event by its id
     *
     * @param id the identifier of the event object
     * @return a ResponseEntity with an HTTP status of ok if the deletion is correct
     */
    @DeleteMapping("delete-event/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id) {
        return eventService.deleteById(id);
    }

    @GetMapping("/filter-by-type")
    public ResponseEntity<?> filterEventsByType(@RequestParam EventType eventType) {
        return eventService.filterEventsByEventType(eventType);
    }

    @GetMapping("/filter-after-date")
    public ResponseEntity<?> filterEventsAfterCertainDate(@RequestParam String date) {
        return eventService.filterEventsAfterCertainDate(date);
    }

    /**
     * This would use something like this: GET htp://localhost:8080/api/events/filter-between-dates?startDate=2025-02-10T00:00:00&endDate=2025-03-01T00:00:00
     * In the frontend.
     * @param startDate String
     * @param endDate String
     * @return ResponseEntity
     */
    @GetMapping("/filter-between-dates")
    public ResponseEntity<?> filterEventsBetweenDates(@RequestParam String startDate, @RequestParam String endDate) {
        return eventService.filterEventsBetweenDates(startDate, endDate);
    }

}
