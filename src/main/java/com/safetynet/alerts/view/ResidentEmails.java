package com.safetynet.alerts.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Class to hold the information for all emails in a city
 */
public class ResidentEmails {
    @Getter @Setter
    private List<String> emails;


    /**
     * Constructor for Resident Emails
     * @param emails
     */
    public ResidentEmails(List<String> emails) {
        this.emails = emails;
    }
}
