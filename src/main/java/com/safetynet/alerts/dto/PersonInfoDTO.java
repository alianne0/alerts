package com.safetynet.alerts.dto;

import lombok.Data;
import java.util.List;

/**
 * Class to hold information for the PersonInfo
 * Includes first and last name, address, age, email,
 * medications, and allergies
 */
//TODO: add @data for getter setter
@Data
public class PersonInfoDTO {
    private String lastName;
    private String firstName;
    private String address;
    private int age;
    private String email;
    private List<String> medications;
    private List<String> allergies;

    public PersonInfoDTO(String lastName, String firstName, String address,
                         int age, String email, List<String> medications, List<String> allergies) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.address = address;
        this.age = age;
        this.email = email;
        this.medications = medications;
        this.allergies = allergies;
    }
}
