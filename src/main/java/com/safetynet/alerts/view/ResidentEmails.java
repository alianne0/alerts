package com.safetynet.alerts.view;

import lombok.Data;
import java.util.List;

/**
 * Class to hold the information for all emails in a city
 */
@Data
public class ResidentEmails {
    private List<String> emails;

    /**
     * Constructor for Resident Emails
     * @param emails
     */
    public ResidentEmails(List<String> emails) {
        this.emails = emails;
    }
}
