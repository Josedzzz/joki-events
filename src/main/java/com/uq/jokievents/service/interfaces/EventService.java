package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.HandleEventDTO;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.model.enums.EventType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EventService {

    ResponseEntity<?> findById(String id);
    ResponseEntity<?> create(Event event);
    ResponseEntity<?> update(String id, Event event);
    ResponseEntity<?> deleteById(String id);
    ResponseEntity<?> getAllEventsPaginated(int page, int size);
    ResponseEntity<?> addEvent(HandleEventDTO dto);
    Optional<Event> getEventById(String eventId);
    void saveEvent(Event event);
    void deleteEventById(String eventId);
    void deleteAllEvents();
    ResponseEntity<?> filterEventsByEventType(EventType eventType);
    ResponseEntity<?> filterEventsAfterCertainDate(String date);
    ResponseEntity<?> filterEventsBetweenDates(String startDate, String endDate);
}
