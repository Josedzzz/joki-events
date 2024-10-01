package com.uq.jokievents.repository;

import com.uq.jokievents.model.Event;
import com.uq.jokievents.model.enums.EventType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByEventType(EventType eventType);
    List<Event> findByEventDateAfter(LocalDateTime dateTime);
    List<Event> findByEventDateBetween(LocalDateTime start, LocalDateTime end);
    Optional<Event> findByLocalitiesName(String localityName);
    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'city': ?1, 'eventDate': { $gte: ?2, $lte: ?3 }, 'eventType': ?4 }")
    List<Event> searchEvents(String eventName, String city, LocalDateTime startDate, LocalDateTime endDate, EventType eventType);

}
