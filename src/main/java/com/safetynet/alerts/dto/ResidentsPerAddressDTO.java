package com.safetynet.alerts.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO to represent the residents per address
 * Includes information such as first and last name, phone, age
 * And a list of medications and allergies
 */
public class ResidentsPerAddressDTO {
    @Getter @Setter
    private String firstName;

    @Getter @Setter
    private String lastName;

    @Getter @Setter
    private String phone;

    @Getter @Setter
    private int age;

    @Getter @Setter
    private List<String> medications;

    @Getter @Setter
    private List<String> allergies;

    /**
     * Construcfor for ResidentsPerAddress DTO
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
