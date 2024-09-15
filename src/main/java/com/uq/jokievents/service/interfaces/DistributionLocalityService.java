package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.model.Locality;
import org.springframework.http.ResponseEntity;

public interface DistributionLocalityService {

    ResponseEntity<?> findAll();
    ResponseEntity<?> findById(String id);
    ResponseEntity<?> create(Locality locality);
    ResponseEntity<?> update(String id, Locality locality);
    ResponseEntity<?> deleteById(String id);

}
