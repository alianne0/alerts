package com.safetynet.alerts.view;

import com.safetynet.alerts.domain.Person;

import java.util.List;

public class PeoplePerStation {
    private List<Person> person;
    private int adultCount;
    private int childCount;

    public PeoplePerStation(List<Person> person, int adultCount, int childCount) {
        this.person = person;
        this.adultCount = adultCount;
        this.childCount = childCount;
    }
    public List<Person> getPerson() {
        return person;
    }

    public void setPerson(List<Person> person) {
        this.person = person;
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


