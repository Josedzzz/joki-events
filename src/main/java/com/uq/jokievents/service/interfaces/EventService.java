package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.model.enums.EventType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public interface EventService {

    ResponseEntity<?> searchEvent(String eventName, String city, LocalDateTime startDate, LocalDateTime endDate, EventType eventType, int page, int size); // todo this is a Service for the Client
    ResponseEntity<?> generateEventsReport(LocalDateTime startDate, LocalDateTime endDate); // todo this is Admin responsibility
}
