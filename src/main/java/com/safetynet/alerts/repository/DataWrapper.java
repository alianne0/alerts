package com.safetynet.alerts.repository;

import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.domain.Person;

import java.util.List;

/**
 * Simple container for the lists stored in the SafetyNet JSON file.
 * Holds persons, firestations, and medical records.
 */
public class DataWrapper {

    private List<Person> persons;
    private List<Firestation> firestations;
    private List<MedicalRecord> medicalrecords;

    /**
     * Returns the list of persons.
     */
    public List<Person> getPersons() { return persons; }

    /**
     * Sets the list of persons.
     */
    public void setPersons(List<Person> persons) { this.persons = persons; }

    /**
     * Returns the list of firestations.
     */
    public List<Firestation> getFirestations() { return firestations; }

    /**
     * Sets the list of firestations.
     */
    public void setFirestations(List<Firestation> firestations) { this.firestations = firestations; }

    /**
     * Returns the list of medical records.
     */
    public List<MedicalRecord> getMedicalrecords() { return medicalrecords; }

    /**
     * Sets the list of medical records.
     */
    public void setMedicalrecords(List<MedicalRecord> medicalrecords) { this.medicalrecords = medicalrecords; }
}