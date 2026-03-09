package com.safetynet.alerts.view;

import com.safetynet.alerts.dto.ResidentsPerAddressDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ResidentsPerAddress {
    @Getter
    @Setter
    private List<ResidentsPerAddressDTO> residents;

    @Getter @Setter
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
