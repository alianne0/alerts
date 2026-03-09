package com.safetynet.alerts.dto;

/**
 * DTO to hold the information of people covered by a specific firestation
 */
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

    /**
     * Returns the first name
     *
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name
     *
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name
     *
     * @return
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name
     *
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the address
     * @return
     */
    public String getAddress()   { return address; }

    /**
     * Sets the address
     * @param address
     */
    public void setAddress(String address)     { this.address = address; }

    /**
     * Returns the phone number
     * @return
     */
    public String getPhone()     { return phone; }

    /**
     * Sets the phone number
     * @param phone
     */
    public void setPhone(String phone)         { this.phone = phone; }

}
