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
