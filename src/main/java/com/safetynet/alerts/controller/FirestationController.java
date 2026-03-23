package com.safetynet.alerts.controller;

import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.service.FirestationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        log.info("Received request: GET /firestations - Fetching all firestations");
        try {
            List<Firestation> result = firestationService.findAll();
            log.debug("Returning {} firestation records", (result != null ? result.size() : 0));
            return result;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching all firestations", ex);
            throw ex;
        }
    }

    /**
     * Delete a firestation mapping by the address
     *
     * @param address
     * @return
     */
    @DeleteMapping("/{address}")
    public ResponseEntity<Void> delete(@PathVariable String address) {
        log.info("Received request: DELETE /firestations/{} - Deleting firestation mapping", address);
        try {
            boolean deleted = firestationService.deleteByAddress(address);

            if (deleted) {
                if (data != null) {
                    data.saveToFile();
                }
                log.info("Successfully deleted firestation mapping for address={}", address);
            } else {
                log.warn("No firestation mapping found to delete for address={}", address);
            }

            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception ex) {
            log.error("Error deleting firestation mapping for address={}", address, ex);
            throw ex;
        }
    }

    /**
     * Add a new firestation with a station and address mapping
     *
     * @param body
     * @return
     */
    @PostMapping()
    public ResponseEntity<Firestation> postFirestation(@RequestBody Firestation body) {
        log.info("Received request: POST /firestations - Adding new firestation");
        try {
            log.debug("Request body received for creation: address={}, station={}",
                    (body != null ? body.getAddress() : null),
                    (body != null ? body.getStation() : null));

            Firestation saved = firestationService.postFirestation(body);
            if (data != null) {
                data.saveToFile();
            }

            log.info("Created firestation: address={}, station={}",
                    (saved != null ? saved.getAddress() : null),
                    (saved != null ? saved.getStation() : null));

            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception ex) {
            log.error("Error creating firestation with body address={} station={}",
                    (body != null ? body.getAddress() : null),
                    (body != null ? body.getStation() : null),
                    ex);
            throw ex;
        }
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

        log.info("Received request: PUT /firestations/address/{} - Updating station number", address);

        if (body == null || body.getStation() == null || body.getStation().trim().isEmpty()) {
            log.warn("Invalid request body for updating firestation at address={}: station field is missing or blank");
            return ResponseEntity.badRequest().body("station must not be null or blank");
        }

        try {
            String trimmedStation = body.getStation().trim();
            log.debug("Attempting update for address={} to station={}", address, trimmedStation);

            return firestationService.updateFirestation(address, trimmedStation)
                    .<ResponseEntity<?>>map(updated -> {
                        if (data != null) {
                            data.saveToFile();
                        }
                        log.info("Successfully updated station for address={} to station={}", address, trimmedStation);
                        return ResponseEntity.ok(updated);
                    })
                    .orElseGet(() -> {
                        log.warn("No firestation mapping found for update at address={}", address);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("No firestation mapping found for address: " + address);
                    });
        } catch (IllegalArgumentException ex) {
            log.error("Bad request while updating firestation at address={}: {}", address, ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while updating station for address={} with body station={}",
                    address, (body != null ? body.getStation() : null), ex);
            throw ex;
        }
    }
}