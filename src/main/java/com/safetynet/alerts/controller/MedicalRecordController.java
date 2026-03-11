package com.safetynet.alerts.controller;

import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.service.MedicalRecordService;
import com.safetynet.alerts.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for the Medical Records
 */
@Slf4j
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
    public MedicalRecordController(MedicalRecordService medicalRecordService,
                                   PersonService personService) {
        this.medicalRecordService = medicalRecordService;
    }

    /**
     * Get all the medical records
     *
     * @return list of medical records
     */
    @GetMapping
    public List<MedicalRecord> getAllMedicalRecords() {
        log.info("GET /medicalRecords - Retrieving all medical records");
        List<MedicalRecord> records = medicalRecordService.findAll();
        log.info("Successfully retrieved {} medical records", records.size());

        return records;
    }

    /**
     * Add a new medical record
     *
     * @param newMedicalRecord
     * @return created medical record
     */
    @PostMapping
    public ResponseEntity<MedicalRecord> postMedicalRecord(
            @RequestBody MedicalRecord newMedicalRecord) {

        log.info("POST /medicalRecords - Creating medical record for {} {}",
                newMedicalRecord.getFirstName(),
                newMedicalRecord.getLastName());

        try {
            MedicalRecord savedMedicalRecord =
                    medicalRecordService.postMedicalRecord(newMedicalRecord);

            log.info("Medical record successfully created for {} {}",
                    savedMedicalRecord.getFirstName(),
                    savedMedicalRecord.getLastName());

            return new ResponseEntity<>(savedMedicalRecord, HttpStatus.CREATED);

        } catch (Exception ex) {
            log.error("Error while creating medical record for {} {}",
                    newMedicalRecord.getFirstName(),
                    newMedicalRecord.getLastName(),
                    ex);
            throw ex;
        }
    }

    /**
     * Update a medical record
     *
     * @param lastName
     * @param firstName
     * @param updates
     * @return updated medical record or error
     */
    @PutMapping("/{lastName}/{firstName}")
    public ResponseEntity<?> updateMedicalRecord(
            @PathVariable String lastName,
            @PathVariable String firstName,
            @RequestBody MedicalRecord updates) {

        log.info("PUT /medicalRecords/{}/{} - Updating medical record",
                lastName, firstName);

        try {
            return medicalRecordService.updateMedicalRecord(lastName, firstName, updates)
                    .<ResponseEntity<?>>map(updated -> {
                        log.info("Medical record updated successfully for {} {}",
                                firstName, lastName);
                        return ResponseEntity.ok(updated);
                    })
                    .orElseGet(() -> {
                        log.warn("Medical record not found for {} {}", firstName, lastName);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("person not found: " + lastName + " " + firstName);
                    });

        } catch (IllegalArgumentException ex) {
            log.error("Invalid update request for {} {}: {}",
                    firstName, lastName, ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Delete a medical record given the name
     *
     * @param lastName
     * @param firstName
     * @return response status
     */
    @DeleteMapping("/{lastName}/{firstName}")
    public ResponseEntity<Void> delete(
            @PathVariable String lastName,
            @PathVariable String firstName) {

        log.info("DELETE /medicalRecords/{}/{} - Deleting medical record",
                lastName, firstName);

        try {
            boolean deleted = medicalRecordService.deleteByName(lastName, firstName);

            if (deleted) {
                log.info("Medical record deleted successfully for {} {}", firstName, lastName);
                return ResponseEntity.ok().build();
            } else {
                log.warn("Medical record not found for deletion: {} {}", firstName, lastName);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception ex) {
            log.error("Error while deleting medical record for {} {}",
                    firstName, lastName, ex);
            throw ex;
        }
    }
}
