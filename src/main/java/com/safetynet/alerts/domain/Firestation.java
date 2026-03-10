package com.safetynet.alerts.domain;

import lombok.Data;

/**
 * Firestation class to hold information such as address and station for a particular firestation
 */
@Data
public class Firestation {
    private String address;
    private String station;
}
