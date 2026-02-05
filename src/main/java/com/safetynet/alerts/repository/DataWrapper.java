package com.safetynet.alerts.repository;

import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.domain.Person;

import java.util.List;

public class DataWrapper {
    private List<Person> persons;
    private List<Firestation> firestations;
    private List<MedicalRecord> medicalrecords;

    public List<Person> getPersons() { return persons; }
    public void setPersons(List<Person> persons) { this.persons = persons; }

    public List<Firestation> getFirestations() { return firestations; }
    public void setFirestations(List<Firestation> firestations) { this.firestations = firestations; }

    public List<MedicalRecord> getMedicalrecords() { return medicalrecords; }
    public void setMedicalrecords(List<MedicalRecord> medicalrecords) { this.medicalrecords = medicalrecords; }
}