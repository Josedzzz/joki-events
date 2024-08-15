package com.uq.jokievents.repository;

import com.uq.jokievents.model.DistributionLocality;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistributionLocalityRepository extends MongoRepository<DistributionLocality, String> {
}
