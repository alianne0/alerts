package com.safetynet.alerts.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for phones per station
 */
public class PhonesPerStationDTO {

    @Getter
    @Setter
    private String phone;

    /**
     * Constructor for phones per station
     * @param phone
     */
    public PhonesPerStationDTO(String phone) {
        this.phone = phone;
    }

}
