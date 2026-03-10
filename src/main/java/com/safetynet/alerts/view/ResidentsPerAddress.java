package com.safetynet.alerts.view;

import com.safetynet.alerts.dto.ResidentsPerAddressDTO;
import lombok.Data;
import java.util.List;

/**
 * Class to hold the information for residents per address
 * Plus the firestation number
 */
@Data
public class ResidentsPerAddress {
    private List<ResidentsPerAddressDTO> residents;
    private String stationNumber;

    /**
     * Constructor for the ResidentsPerAddress view
     * @param residents
     * @param stationNumber
     */
    public ResidentsPerAddress(List<ResidentsPerAddressDTO> residents, String stationNumber) {
        this.residents = residents;
        this.stationNumber = stationNumber;
    }
}
