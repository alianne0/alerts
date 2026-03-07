package com.safetynet.alerts.domain;

/**
 * Person class to hold the information for a person, such as first name, last name, address, city, zip, phone, email
 */
public class Person {
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String zip;
    private String phone;
    private String email;

    /**
     * Returns the first name
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name
     * @return
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the address
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address
     * @param address
     */
    public void setAddress(String address){
        this.address = address;
    }

    /**
     * Returns the city
     * @return
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city
     * @param city
     */
    public void setCity(String city){
        this.city = city;
    }

    /**
     * Returns the zip code
     * @return
     */
    public String getZip() {
        return zip;
    }

    /**
     * Sets the zip code
     * @param zip
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * Returns the phone
     * @return
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the email
     * @return
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }
}

