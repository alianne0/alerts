package com.safetynet.alerts.controller;

import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.service.MedicalRecordService;
import com.safetynet.alerts.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for the Medical Records
 */
@RestController
@RequestMapping("/medicalRecords")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    /**
     * Constructor for the Medical Record controller
     *
     * @param medicalRecordService
     * @param personService
     */
    @Autowired
    public MedicalRecordController(MedicalRecordService medicalRecordService, PersonService personService) {
        this.medicalRecordService = medicalRecordService;
    }

    /**
     * Get all the medical records
     *
     * @return
     */
    @GetMapping
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordService.findAll();
    }

    /**
     * Add a new medical record
     *
     * @param newMedicalRecord
     * @return
     */
    @PostMapping
    public ResponseEntity<MedicalRecord> postMedicalRecord(@RequestBody MedicalRecord newMedicalRecord) {
        MedicalRecord savedMedicalRecord = medicalRecordService.postMedicalRecord(newMedicalRecord);
        return new ResponseEntity<>(savedMedicalRecord, HttpStatus.CREATED);
    }

    /**
     * Update a medical record
     *
     * @param lastName
     * @param firstName
     * @param updates
     * @return
     */
    @PutMapping("/{lastName}/{firstName}")
    public ResponseEntity<?> updateMedicalRecord (
            @PathVariable String lastName,
            @PathVariable String firstName,
            @RequestBody MedicalRecord updates) {
        try {
            return medicalRecordService.updateMedicalRecord(lastName, firstName, updates)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("person not found: " + lastName + "" + firstName));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Delete a medical record given the name
     *
     * @param lastName
     * @param firstName
     * @return
     */
    @DeleteMapping("/{lastName}/{firstName}")
    public ResponseEntity<Void> delete (
            @PathVariable String lastName, @PathVariable String firstName) {
        boolean deleted = medicalRecordService.deleteByName(lastName, firstName);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
