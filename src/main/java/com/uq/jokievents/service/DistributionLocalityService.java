package com.uq.jokievents.service;

import com.uq.jokievents.model.DistributionLocality;
import com.uq.jokievents.repository.DistributionLocalityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DistributionLocalityService {

    @Autowired
    private DistributionLocalityRepository distributionLocalityRepository;

    /**
     * Get a list of all distributionLocality from the db
     *
     * @return a list of all distributionLocality objects in the db
     */
    public List<DistributionLocality> findAll() {
        return distributionLocalityRepository.findAll();
    }

    /**
     * Gets a report by its id from the db
     *
     * @param id the identifier of the distributionLocality
     * @return an Optional containing thedistributionLocality if found, empty Optional if not
     */
    public Optional<DistributionLocality> findById(String id) {
        return distributionLocalityRepository.findById(id);
    }

    /**
     * Saves a new distributionLocality or updates an existing in the db
     *
     * @param distributionLocality the distributionLocality object to be saves or updated
     * @return the saved or updated report object
     */
    public DistributionLocality save(DistributionLocality distributionLocality) {
        return distributionLocalityRepository.save(distributionLocality);
    }

    /**
     * Deletes a distributionLocality from the db using its id
     *
     * @param id the identifier of the distributionLocality to be deleted
     */
    public void deleteById(String id) {
        distributionLocalityRepository.deleteById(id);
    }

}
