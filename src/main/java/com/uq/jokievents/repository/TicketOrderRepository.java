package com.uq.jokievents.repository;

import com.uq.jokievents.model.TicketOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketOrderRepository extends MongoRepository<TicketOrder, String> {
}
