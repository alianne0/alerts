package com.safetynet.alerts.domain;

import java.util.List;

/**
 * Medical record class which holds information such as first name, last name, birthdate,
 * and any list of medications or alleries
 */
public class MedicalRecord {

    private String firstName;
    private String lastName;
    private String birthdate;

    private List<String> medications;
    private List<String> allergies;

    /**
     * Return the first name
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Return the last name
     * @return
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the last name
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Return the birthdate
     * @return
     */
    public String getBirthdate() {
        return birthdate;
    }

    /**
     * Sets the birthdate
     * @param birthdate
     */
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    /**
     * Returns the medications
     * @return
     */
    public List<String> getMedications() {
        return medications;
    }

    /**
     * Sets the medictions
     * @param medications
     */
    public void setMedications(List<String> medications) {
        this.medications = medications;
    }

    /**
     * Returns the allergies
     * @return
     */
    public List<String> getAllergies() {
        return allergies;
    }

    /**
     * Sets the allergies
     * @param allergies
     */
    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }
}