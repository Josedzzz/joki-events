package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.HandleEventDTO;
import com.uq.jokievents.dtos.ReportEventDTO;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.model.enums.EventType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventService {

    ResponseEntity<?> getAllEventsPaginated(int page, int size);
    ResponseEntity<?> addEvent(HandleEventDTO dto);
    ResponseEntity<?> updateEvent(String eventId, HandleEventDTO dto);
    Optional<Event> getEventById(String eventId);
    void saveEvent(Event event);
    void deleteEventById(String eventId);
    void deleteAllEvents();
    ResponseEntity<?> filterEventsAfterCertainDate(String date, int page, int size); // 2
    ResponseEntity<?> filterEventsBetweenDates(String startDate, String endDate, int page, int size); // 3
    Optional<Event> findByEventById(String eventId);
    Optional<Event> findEventByLocalityName(String localityName);
    ResponseEntity<?> searchEvent(String eventName, String city, LocalDateTime startDate, LocalDateTime endDate, EventType eventType, int page, int size);
    ResponseEntity<?> generateEventsReport(LocalDateTime startDate, LocalDateTime endDate);
}
