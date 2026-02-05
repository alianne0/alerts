package com.safetynet.alerts.controller;

import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.service.FirestationService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/firestation")
public class FirestationController {

    private final DataParser data;
    private final FirestationService firestationService;

    @Autowired
    public FirestationController(DataParser data, FirestationService firestationService) {
        this.data = data;
        this.firestationService = firestationService;
    }

    @GetMapping
    public List<Firestation> getFirestations() {
        return firestationService.findAll();
    }

    @DeleteMapping("/{address}")
    public ResponseEntity<Void> delete(
            @PathVariable String address) {
        boolean deleted = firestationService.deleteByAddress(address);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

//    @DeleteMapping("/station")
//    public ResponseEntity<Void> delete(
//            @PathVariable String station) {
//        boolean deleted = firestationService.deleteByStation(station);
//        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
//    }


    @PatchMapping("/address/{address}")
    public ResponseEntity<?> patchStationForAddress(
            @PathVariable String address,
            @RequestBody Firestation body) {

        if (body == null || body.getStation() == null || body.getStation().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("station must not be null or blank");
        }
        try {
            return firestationService.updateFirestation(address, body.getStation().trim())
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("No firestation mapping found for address: " + address));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
