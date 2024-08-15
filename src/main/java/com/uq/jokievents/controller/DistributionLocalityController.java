package com.uq.jokievents.controller;

import com.uq.jokievents.model.DistributionLocality;
import com.uq.jokievents.service.DistributionLocalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<List<DistributionLocality>> getAllDistributionLocality() {
        List<DistributionLocality> distributionLocalities = distributionLocalityService.findAll();
        return new ResponseEntity<>(distributionLocalities, HttpStatus.OK);
    }

    /**
     * Gets a distributionLocality by its id
     *
     * @param id the identifier of the distributionLocality object
     * @return a ResponseEntity containing the distributionLocality object and an HTTP status of ok if found, otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<DistributionLocality> getDistributionLocalityById(@PathVariable String id) {
        Optional<DistributionLocality> report = distributionLocalityService.findById(id);
        return report.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new distributionLocality
     *
     * @param distributionLocality the distributionLocality object to be saved
     * @return a ResponseEntity containing the created distributionLocality object and an HTTP status of created
     */
    @PostMapping
    public ResponseEntity<DistributionLocality> createReport(@RequestBody DistributionLocality distributionLocality) {
        DistributionLocality newdistributionLocality = distributionLocalityService.save(distributionLocality);
        return new ResponseEntity<>(newdistributionLocality, HttpStatus.CREATED);
    }

    /**
     * Updates an existing distributionLocality
     *
     * @param id the identifier of the distributionLocality to be updated
     * @param distributionLocality the distributionLocality object containing the update data
     * @return a ResponseEntity containing the updated distributionLocality object and an HTTP status of ok, otherwise not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<DistributionLocality> updateReport(@PathVariable String id, @RequestBody DistributionLocality distributionLocality) {
        Optional<DistributionLocality> existingDistributionLocality = distributionLocalityService.findById(id);
        if (existingDistributionLocality.isPresent()) {
            distributionLocality.setId(id);
            DistributionLocality updatedDistributionLocality = distributionLocalityService.save(distributionLocality);
            return new ResponseEntity<>(updatedDistributionLocality, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes a distributionLocality by its id
     *
     * @param id the identifier of the distributionLocality object to be deleted
     * @return a ResponseEntity with an HTTP status of ok if the deletion is succesful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDistributionLocality(@PathVariable String id) {
        distributionLocalityService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
