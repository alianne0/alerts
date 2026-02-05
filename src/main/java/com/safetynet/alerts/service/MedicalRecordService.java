package com.safetynet.alerts.service;

import com.safetynet.alerts.controller.MedicalRecordController;
import com.safetynet.alerts.domain.MedicalRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.safetynet.alerts.repository.DataParser;

import java.util.*;

@Service
public class MedicalRecordService {
    private final DataParser data;

    @Autowired
    public MedicalRecordService(DataParser data) {
        this.data = data;
    }

    public List<MedicalRecord> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(data.getMedicalRecords()));
    }

    // Add a medical record
//    public MedicalRecord postMedicalRecord(MedicalRecord medicalRecord) {
//        Objects.requireNonNull(medicalRecord, "medical record cannot be empty");
//        List<MedicalRecord> medicalRecords = data.getMedicalRecords();
//
//        for (int i = 0; i < medicalRecords.size(); i++) {
//            MedicalRecord existing  = medicalRecords.get(i);
//            if(equalsTrimmed(existing.getFirstName(), m))
//        }
//    }

    //Update an existing medial record but dont change the firstname and lastname
    public Optional <MedicalRecord> updateMedicalRecord(String firstName, String lastName, MedicalRecord updates) {
        Objects.requireNonNull(updates, "updates cant be null");
        if (updates.getFirstName() != null && !equalsTrimmed(updates.getFirstName(), firstName)){
            throw new IllegalArgumentException("firstname cannot be changed");
        }
        if (updates.getLastName() != null && !equalsTrimmed(updates.getLastName(), lastName)) {
            throw new IllegalArgumentException("lastname cannot be changed");
        }

        for(MedicalRecord existing : data.getMedicalRecords()) {
            if (equalsTrimmed(existing.getFirstName(), firstName) && equalsTrimmed(existing.getLastName(), lastName)) {
                if (updates.getMedications() != null) {
                    existing.setMedications(updates.getMedications());
                }
                if (updates.getAllergies() != null) {
                    existing.setAllergies(updates.getMedications());
                }
                if(updates.getBirthdate() !=null) {
                    existing.setBirthdate(updates.getBirthdate());
                }
            }
        }
        return Optional.empty();
    }

    //Delete a medical record, query by firstname and lastname
    public boolean deleteByName(String firstName, String lastName) {
        return data.getMedicalRecords().removeIf(
                m -> equalsTrimmed(m.getFirstName(), firstName)
                && equalsTrimmed(m.getLastName(), lastName)
        );
    }

    private boolean equalsTrimmed(String a, String b) {
        String aa = (a == null) ? null : a.trim();
        String bb = (b == null) ? null : b.trim();
        return Objects.equals(aa, bb);
    }
}
