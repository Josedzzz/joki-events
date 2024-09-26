package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.HandleEventDTO;
import com.uq.jokievents.model.Event;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface EventService {

    ResponseEntity<?> findAll();
    ResponseEntity<?> findById(String id);
    ResponseEntity<?> create(Event event);
    ResponseEntity<?> update(String id, Event event);
    ResponseEntity<?> deleteById(String id);
    ResponseEntity<?> getAllEventsPaginated(int page, int size);
    ResponseEntity<?> addEvent(HandleEventDTO dto); // Long life to SRP
    Optional<Event> getEventById(String eventId);
    void deleteEventById(String eventId); // Loooong life to SRP
    void deleteAllEvents();
}
