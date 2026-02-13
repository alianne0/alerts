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

    public MedicalRecord postMedicalRecord(MedicalRecord medicalRecord) {
        Objects.requireNonNull(medicalRecord,"medical record cannot be null");
        List<MedicalRecord> medicalRecords = data.getMedicalRecords();

        for(int i = 0; i < medicalRecords.size(); i++) {
            MedicalRecord existing = medicalRecords.get(i);
            if(equalsTrimmed(existing.getLastName(), medicalRecord.getLastName())
            && equalsTrimmed(existing.getFirstName(), medicalRecord.getFirstName())){
                existing.setFirstName(medicalRecord.getFirstName());
                existing.setLastName(medicalRecord.getLastName());
                existing.setBirthdate(medicalRecord.getBirthdate());
                existing.setMedications(medicalRecord.getMedications());
                existing.setAllergies(medicalRecord.getAllergies());
                return existing;
            }
        }
        medicalRecords.add(medicalRecord);
        return medicalRecord;
    }

    public Optional <MedicalRecord> updateMedicalRecord(String lastName, String firstName, MedicalRecord updates) {
        Objects.requireNonNull(updates, "updates cant be null");
        if (updates.getLastName() != null && !equalsTrimmed(updates.getLastName(), lastName)){
            throw new IllegalArgumentException("last name cannot be changed");
        }
        if (updates.getFirstName() != null && !equalsTrimmed(updates.getFirstName(), firstName)) {
            throw new IllegalArgumentException("first name cannot be changed");
        }
        for(MedicalRecord existing : data.getMedicalRecords()) {
            if (equalsTrimmed(existing.getLastName(), lastName) && equalsTrimmed(existing.getFirstName(), firstName)) {
                if (updates.getMedications() != null) {
                    existing.setMedications(updates.getMedications());
                }
                if (updates.getAllergies() != null) {
                    existing.setAllergies(updates.getAllergies());
                }
                if(updates.getBirthdate() !=null) {
                    existing.setBirthdate(updates.getBirthdate());
                    return Optional.of(existing);
                }
            }
        }
        return Optional.empty();
    }

    public boolean deleteByName(String lastName, String firstName) {
        return data.getMedicalRecords().removeIf(
                m -> equalsTrimmed(m.getLastName(), lastName)
                && equalsTrimmed(m.getFirstName(), firstName)
        );
    }

    private boolean equalsTrimmed(String a, String b) {
        String aa = (a == null) ? null : a.trim();
        String bb = (b == null) ? null : b.trim();
        return Objects.equals(aa, bb);
    }
}
