package com.uq.jokievents.repository;

import com.uq.jokievents.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    boolean existsByEventDate(LocalDateTime eventDate);
    boolean existsByAddress(String address);
    boolean existsByCity(String city);
}
