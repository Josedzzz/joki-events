package com.uq.jokievents.repository;

import com.uq.jokievents.model.Purchase;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PurchaseRepository extends MongoRepository<Purchase, String> {
    List<Purchase> findByClientId(String clientId);
}
