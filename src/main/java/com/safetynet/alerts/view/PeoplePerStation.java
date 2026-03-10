package com.safetynet.alerts.view;

import com.safetynet.alerts.dto.CoveredPersonsDTO;
import lombok.Data;
import java.util.List;

/**
 * Class to hold information about the covered persons per station
 * and also holds the adult and child count associated with that
 */
@Data
public class PeoplePerStation {
    private List<CoveredPersonsDTO> coveredPersons;
    private int adultCount;
    private int childCount;

    /**
     * Constructor for the PeoplePerStation view
     * @param coveredPersons
     * @param adultCount
     * @param childCount
     */
    public PeoplePerStation(List<CoveredPersonsDTO> coveredPersons, int adultCount, int childCount) {
        this.coveredPersons = coveredPersons;
        this.adultCount = adultCount;
        this.childCount = childCount;
    }
}


