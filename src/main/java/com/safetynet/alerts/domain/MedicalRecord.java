package com.safetynet.alerts.domain;

import lombok.Data;

import java.util.List;

/**
 * Medical record class which holds information such as first name, last name, birthdate,
 * and any list of medications or allergies
 */
@Data
public class MedicalRecord {
    private String firstName;
    private String lastName;
    private String birthdate;
    private List<String> medications;
    private List<String> allergies;
}