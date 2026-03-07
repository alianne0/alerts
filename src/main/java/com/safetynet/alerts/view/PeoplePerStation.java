package com.safetynet.alerts.view;

import com.safetynet.alerts.dto.CoveredPersonsDTO;

import java.util.List;

public class PeoplePerStation {
    private List<CoveredPersonsDTO> coveredPersons;
    private int adultCount;
    private int childCount;

    public PeoplePerStation(List<CoveredPersonsDTO> coveredPersons, int adultCount, int childCount) {
        this.coveredPersons = coveredPersons;
        this.adultCount = adultCount;
        this.childCount = childCount;
    }
    public List<CoveredPersonsDTO> getCoveredPersons() {
        return coveredPersons;
    }

    public void setCoveredPersons(List<CoveredPersonsDTO> coveredPersons) {
        this.coveredPersons = coveredPersons;
    }

    public int getAdultCount() {
        return adultCount;
    }

    public void setAdultCount(int adultCount) {
        this.adultCount = adultCount;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }
}


