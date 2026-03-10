package com.safetynet.alerts.view;

import lombok.Data;
import java.util.List;

/**
 * Class to store information on the list of phones per station
 */
@Data
public class PhonesPerStation {
    private List<String> phones;

    /**
     * Constructor for the phones per station class
     * @param phones
     */
    public PhonesPerStation(List<String> phones) {
    this.phones = phones;
    }

}
