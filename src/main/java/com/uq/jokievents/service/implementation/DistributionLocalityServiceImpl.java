package com.uq.jokievents.service.implementation;

import com.uq.jokievents.model.Locality;
import com.uq.jokievents.repository.LocalityRepository;
import com.uq.jokievents.service.interfaces.DistributionLocalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DistributionLocalityServiceImpl implements DistributionLocalityService {

    @Autowired
    private LocalityRepository localityRepository;

    /**
     * Get a list of all distributionLocality from the db
     *
     * @return a list of all distributionLocality objects in the db
     */
    public ResponseEntity<?> findAll() {
        try {
            List<Locality> locality = localityRepository.findAll();
            return new ResponseEntity<>(locality, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed distributionLocality request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets a report by its id from the db
     *
     * @param id the identifier of the distributionLocality
     * @return an Optional containing thedistributionLocality if found, empty Optional if not
     */
    public ResponseEntity<?> findById(String id) {
        try {
            Optional<Locality> distributionLocality = localityRepository.findById(id);
            if (distributionLocality.isPresent()) {
                return new ResponseEntity<>(distributionLocality.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("DistributionLocality not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed distributionLocality request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Saves a new distributionLocality or updates an existing in the db
     *
     * @param locality the distributionLocality object to be saves or updated
     * @return the saved or updated report object
     */
    public ResponseEntity<?> create(Locality locality) {
        try {
            Locality createdLocality = localityRepository.save(locality);
            return new ResponseEntity<>(createdLocality, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create DistributionLocality", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing client by id
     *
     * @param id the identifierof the DistributionLocality to be update
     * @param locality the updated DistributionLocality object
     * @return a ResponseEntity containing the updated DistributionLocality object and an HTTP status
     */
    public ResponseEntity<?> update(String id, Locality locality) {
        try {
            Optional<Locality> existingDistributionLocality = localityRepository.findById(id);
            if (existingDistributionLocality.isPresent()) {
                locality.setId(id);
                Locality updatedLocality = localityRepository.save(locality);
                return new ResponseEntity<>(updatedLocality, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("DistributionLocality not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to DistributionLocality client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a distributionLocality from the db using its id
     *
     * @param id the identifier of the distributionLocality to be deleted
     */
    public ResponseEntity<?> deleteById(String id) {
        try {
            Optional<Locality> existingDistributionLocality = localityRepository.findById(id);
            if (existingDistributionLocality.isPresent()) {
                localityRepository.deleteById(id);
                return new ResponseEntity<>("DistributionLocality deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("DistributionLocality not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

