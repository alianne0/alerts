package com.safetynet.alerts.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO to represent the residents per address
 * Includes information such as first and last name, phone, age
 * And a list of medications and allergies
 */
@Data
public class ResidentsPerAddressDTO {
    private String firstName;
    private String lastName;
    private String phone;
    private int age;
    private List<String> medications;
    private List<String> allergies;

    /**
     * Constructor for ResidentsPerAddress DTO
     * @param firstName
     * @param lastName
     * @param phone
     * @param age
     * @param medications
     * @param allergies
     */
    public ResidentsPerAddressDTO(String firstName, String lastName, String phone, int age, List<String> medications, List<String> allergies) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.age = age;
        this.medications = medications;
        this.allergies = allergies;
    }
}
