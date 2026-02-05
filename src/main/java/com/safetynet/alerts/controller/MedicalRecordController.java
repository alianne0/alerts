package com.safetynet.alerts.controller;

import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.service.MedicalRecordService;
import com.safetynet.alerts.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicalRecord")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    @Autowired
    public MedicalRecordController(MedicalRecordService medicalRecordService, PersonService personService) {
        this.medicalRecordService = medicalRecordService;
    }
    @GetMapping
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordService.findAll();
    }

    //update an existing medical record but dont change firstname and lastname
    @PatchMapping("/{firstName}/{lastName}")
    public ResponseEntity<?> patchMedicalRecord (
            @PathVariable String firstName,
            @PathVariable String lastName,
            @RequestBody MedicalRecord updates) {
        try {
            return medicalRecordService.updateMedicalRecord(firstName, lastName, updates)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("person not found: " + firstName + "" + lastName));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    //delete a medical record
    @DeleteMapping("/{firstName}/{lastName}")
    public ResponseEntity<Void> delete (
            @PathVariable String firstName, @PathVariable String lastName) {
        boolean deleted = medicalRecordService.deleteByName(firstName, lastName);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    //finish the update a medical record

}
