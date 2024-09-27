package com.uq.jokievents.repository;

import com.uq.jokievents.model.LocalityOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends MongoRepository<LocalityOrder, String> {
}
