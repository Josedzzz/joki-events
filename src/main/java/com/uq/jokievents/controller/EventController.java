package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.SearchEventDTO;
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

    @GetMapping("/filter-after-date")
    public ResponseEntity<?> filterEventsAfterCertainDate(@RequestParam String date, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "16") int size) {
        return eventService.filterEventsAfterCertainDate(date, page, size);
    }

    /**
     * This would use something like this: GET http://localhost:8080/api/events/filter-between-dates?startDate=2025-02-10T00:00:00&endDate=2025-03-01T00:00:00
     * In Postman.
     * @param startDate String
     * @param endDate String
     * @return ResponseEntity
     */
    @GetMapping("/filter-between-dates")
    public ResponseEntity<?> filterEventsBetweenDates(@RequestParam String startDate, @RequestParam String endDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "16") int size) {
        return eventService.filterEventsBetweenDates(startDate, endDate, page, size);
    }

    @PostMapping("/search-event")
    public ResponseEntity<?> searchEvent(@RequestBody SearchEventDTO dto, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "16") int size) {

        return eventService.searchEvent(
                dto.eventName(),
                dto.city(),
                dto.startDate(),
                dto.endDate(),
                dto.eventType(),
                page,
                size
        );
    }
}
