package com.safetynet.alerts.dto;

import lombok.Data;

/**
 * DTO to hold the information of people covered by a specific firestation
 */
@Data
public class CoveredPersonsDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String phone;

    /**
     * Constructor for the covered persons DTO
     * @param firstName
     * @param lastName
     * @param address
     * @param phone
     */
    public CoveredPersonsDTO(String firstName, String lastName, String address, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
    }
}
