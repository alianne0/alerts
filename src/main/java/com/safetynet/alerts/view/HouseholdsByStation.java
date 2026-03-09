package com.safetynet.alerts.view;

import com.safetynet.alerts.dto.ResidentsPerAddressDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Class/view to hold information such as the list of residents and their
 * respective addresses
 */
public class HouseholdsByStation {
    @Getter @Setter
    private List<ResidentsPerAddressDTO> residents;

    @Getter @Setter
    private String address;


    /**
     * Constructor for the HouseholdsByStation view
     * @param address
     * @param residents
     */
    public HouseholdsByStation(String address, List<ResidentsPerAddressDTO> residents){
        this.address = address;
        this.residents = residents;
    }
}
