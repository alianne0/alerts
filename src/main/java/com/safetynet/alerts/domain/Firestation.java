package com.safetynet.alerts.domain;

/**
 * Firestation class to hold information such as address and station for a particular firestation
 */
public class Firestation {
    private String address;
    private String station;

    /**
     * Returns the address
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address
     * @param address
     */
    public void setAddress(String address){
        this.address = address;
    }

    /**
     * Returns the station
     * @return
     */
    public String getStation() {
        return station;
    }

    /**
     * Sets the station
     * @param station
     */
    public void setStation(String station) {
        this.station = station;
    }
}
