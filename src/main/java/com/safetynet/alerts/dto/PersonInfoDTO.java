package com.safetynet.alerts.dto;

import com.safetynet.alerts.view.PersonInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Class to hold information for the PersonInfo
 * Includes first and last name, address, age, email,
 * medications, and allergies
 */
public class PersonInfoDTO {
    @Getter @Setter
    private String lastName;

    @Getter @Setter
    private String firstName;

    @Getter @Setter
    private String address;

    @Getter @Setter
    private int age;

    @Getter @Setter
    private String email;

    @Getter @Setter
    private List<String> medications;

    @Getter @Setter
    private List<String> allergies;

    public PersonInfoDTO(String lastName, String firstName, String address,
                         int age, String email, List<String> medications, List<String> allergies) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.address = address;
        this. age = age;
        this.email = email;
        this.medications = medications;
        this.allergies = allergies;
    }
}
