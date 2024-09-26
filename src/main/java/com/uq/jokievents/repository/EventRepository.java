package com.uq.jokievents.repository;

import com.uq.jokievents.model.Event;
import com.uq.jokievents.model.enums.EventType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByEventType(EventType eventType);
    List<Event> findByEventDateAfter(LocalDateTime dateTime);
    List<Event> findByEventDateBetween(LocalDateTime start, LocalDateTime end);
}
