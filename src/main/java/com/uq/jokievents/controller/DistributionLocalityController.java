package com.uq.jokievents.controller;

import com.uq.jokievents.model.DistributionLocality;
import com.uq.jokievents.service.interfaces.DistributionLocalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/distributionlocalities")
public class DistributionLocalityController {

    @Autowired
    private DistributionLocalityService distributionLocalityService;

    /**
     * Gets a list of all distributionLocality
     *
     * @return a ResponseEntity containing a list of distributionLocality objects and an HTTP status of ok
     */
    @GetMapping
    public ResponseEntity<?> getAllDistributionLocality() {
        return distributionLocalityService.findAll();
    }

    /**
     * Gets a distributionLocality by its id
     *
     * @param id the identifier of the distributionLocality object
     * @return a ResponseEntity containing the distributionLocality object and an HTTP status of ok if found, otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDistributionLocalityById(@PathVariable String id) {
        return distributionLocalityService.findById(id);
    }

    /**
     * Creates a new distributionLocality
     *
     * @param distributionLocality the distributionLocality object to be saved
     * @return a ResponseEntity containing the created distributionLocality object and an HTTP status of created
     */
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody DistributionLocality distributionLocality) {
        return distributionLocalityService.create(distributionLocality);
    }

    /**
     * Updates an existing distributionLocality
     *
     * @param id the identifier of the distributionLocality to be updated
     * @param distributionLocality the distributionLocality object containing the update data
     * @return a ResponseEntity containing the updated distributionLocality object and an HTTP status of ok, otherwise not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReport(@PathVariable String id, @RequestBody DistributionLocality distributionLocality) {
        return distributionLocalityService.update(id, distributionLocality);
    }

    /**
     * Deletes a distributionLocality by its id
     *
     * @param id the identifier of the distributionLocality object to be deleted
     * @return a ResponseEntity with an HTTP status of ok if the deletion is succesful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDistributionLocality(@PathVariable String id) {
        return distributionLocalityService.deleteById(id);
    }
}
