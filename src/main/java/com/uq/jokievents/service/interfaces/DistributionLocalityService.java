package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.model.DistributionLocality;
import org.springframework.http.ResponseEntity;

public interface DistributionLocalityService {

    ResponseEntity<?> findAll();
    ResponseEntity<?> findById(String id);
    ResponseEntity<?> create(DistributionLocality distributionLocality);
    ResponseEntity<?> update(String id, DistributionLocality distributionLocality);
    ResponseEntity<?> deleteById(String id);

}
