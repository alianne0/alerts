package com.safetynet.alerts.view;

import com.safetynet.alerts.dto.ResidentsPerAddressDTO;
import lombok.Data;
import java.util.List;

/**
 * Class/view to hold information such as the list of residents and their
 * respective addresses
 */
@Data
public class HouseholdsByStation {
    private List<ResidentsPerAddressDTO> residents;
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
