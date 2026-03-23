package com.safetynet.alerts.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO for get children by address
 */
@Data
public class ChildrenByAddressDTO {
    private String firstName;
    private String lastName;
    private int age;
    private List<HouseholdMembersDTO> householdMembersList;

    /**
     * Constructor for children by address DTO
     *
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
