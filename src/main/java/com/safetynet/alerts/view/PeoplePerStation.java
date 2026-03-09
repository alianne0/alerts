package com.safetynet.alerts.view;

import com.safetynet.alerts.dto.CoveredPersonsDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Class to hold information about the covered persons per station
 * and also holds the adult and child count associated with that
 */
public class PeoplePerStation {
    @Getter @Setter
    private List<CoveredPersonsDTO> coveredPersons;
    @Getter @Setter
    private int adultCount;
    @Getter @Setter
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


