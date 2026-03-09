package com.safetynet.alerts.view;

import com.safetynet.alerts.dto.PhonesPerStationDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Class to store information on the list of phones per station
 */
public class PhonesPerStation {

    @Getter
    @Setter
    private List<String> phones;

    /**
     * Constructor for the phones per station class
     * @param phones
     */
    public PhonesPerStation(List<String> phones) {
    this.phones = phones;
    }

}
