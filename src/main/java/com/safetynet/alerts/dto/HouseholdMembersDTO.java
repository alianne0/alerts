package com.safetynet.alerts.dto;

import lombok.Data;

/**
 * DTO to hold the information for household members of children for a specific station
 */
@Data
public class HouseholdMembersDTO {
    private String firstName;
    private String lastName;

    /**
     * Constructor for household members
     *
     * @param firstName
     * @param lastName
     */
    public HouseholdMembersDTO(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
