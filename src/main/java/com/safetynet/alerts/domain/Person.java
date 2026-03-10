package com.safetynet.alerts.domain;

import lombok.Data;

/**
 * Person class to hold the information for a person, such as first name, last name, address, city, zip, phone, email
 */
@Data
public class Person {
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String zip;
    private String phone;
    private String email;
}

