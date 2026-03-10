package com.safetynet.alerts.controller;

import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.service.FirestationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for the Firestation
 */
@Slf4j
@RestController
@RequestMapping("/firestations")
public class FirestationController {

    private final DataParser data;
    private final FirestationService firestationService;

    /**
     * Constructor for the Firestation Controller
     *
     * @param data
     * @param firestationService
     */
    @Autowired
    public FirestationController(DataParser data, FirestationService firestationService) {
        this.data = data;
        this.firestationService = firestationService;
    }

    /**
     * Get all firestations
     *
     * @return
     */
    @GetMapping
    public List<Firestation> getFirestations() {
        log.info("Getting all firestation data...");
        return firestationService.findAll();
    }

    /**
     * Delete a firestation mapping by the address
     *
     * @param address
     * @return
     */
    @DeleteMapping("/{address}")
    public ResponseEntity<Void> delete(
            @PathVariable String address) {
        boolean deleted = firestationService.deleteByAddress(address);
        log.info("Deleting a firestation...");
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Add a new firestation with a station and address mapping
     *
     * @param body
     * @return
     */
    @PostMapping()
    public ResponseEntity<Firestation> postFirestation(@RequestBody Firestation body) {
        log.info("Adding a new firestation...");
        Firestation saved = firestationService.postFirestation(body);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }


    /**
     * Update a firestation's station number by providing its address
     *
     * @param address
     * @param body
     * @return
     */
    @PutMapping(path = "/address/{address}")
    public ResponseEntity<?> putStationForAddress(
            @PathVariable String address,
            @RequestBody Firestation body) {

        if (body == null || body.getStation() == null || body.getStation().trim().isEmpty()) {
            log.info("Could not update a firestation due to bad input");
            return ResponseEntity.badRequest().body("station must not be null or blank");
        }
        try {
            log.info("Updating a firestations number...");
            return firestationService.updateFirestation(address, body.getStation().trim())
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("No firestation mapping found for address: " + address));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
