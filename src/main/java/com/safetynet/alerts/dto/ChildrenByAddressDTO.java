package com.safetynet.alerts.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO for get children by address
 */
public class ChildrenByAddressDTO {
    /**
     * Sets and gets the firstname
     */
    @Setter
    @Getter
    private String firstName;
    /**
     * Sets and gets the lastname
     *
     * @param lastName
     */
    @Setter
    @Getter
    private String lastName;

    /**
     * Sets and gets the age
     * @param age
     */
    @Setter
    @Getter
    private int age;

    /**
     * Gets and sets the household members list
     */
    @Getter @Setter
    private List<HouseholdMembersDTO> householdMembersList;

    /**
     * Construcfor for children by address DTO
     * @param firstName
     * @param lastName
     * @param age
     */
    public ChildrenByAddressDTO(String firstName, String lastName, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }
}
