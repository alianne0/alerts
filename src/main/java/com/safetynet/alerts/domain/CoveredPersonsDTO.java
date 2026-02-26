package com.safetynet.alerts.domain;

public class CoveredPersonsDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String phone;

    public CoveredPersonsDTO(String firstName, String lastName, String address, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
    }
}
