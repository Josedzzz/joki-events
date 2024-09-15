package com.uq.jokievents.repository;

import com.uq.jokievents.model.Locality;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalityRepository extends MongoRepository<Locality, String> {
}
