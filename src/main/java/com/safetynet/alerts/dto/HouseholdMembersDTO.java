package com.safetynet.alerts.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO to hold the information for household members of children for a specific station
 */
public class HouseholdMembersDTO {
    @Getter
    @Setter
    private String firstName;

    @Getter
    @Setter
    private String lastName;

    /**
     * Constructor for household members
     * @param firstName
     * @param lastName
     */
    public HouseholdMembersDTO(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
