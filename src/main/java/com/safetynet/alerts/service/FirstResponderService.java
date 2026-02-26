package com.safetynet.alerts.service;

import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.view.PeoplePerStation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.domain.CoveredPersonsDTO;
import com.safetynet.alerts.domain.FireStationResponseDTO;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

@Service
public class FirstResponderService {
    private final DataParser data;

    @Autowired
    public FirstResponderService(DataParser data) { this.data = data; }


    public PeoplePerStation getPeopleByStation(String stationNumber) {
        List<String> addresses = new ArrayList<>();

        // find all addresses for this firestation
        for(Firestation fs : data.getFirestations()){
            if(fs.getStation().equals(stationNumber)) {
                addresses.add(fs.getAddress());
            }
        }
        //filter persons who live at those addresses
        //map them to DTO
        //count adults and children (requires medical records)
    }

    /**
     * filter (projecting part od domain object into this object (logic done here)
     * get birthday is accessing the medical record service and accessing a specific persons
     * if the address tied to the station matches the address tied to a person, then that person will be added to the person count
     * if person is a certain age, then add to child or adult count
     * get info needed for the view
     * compare person and service
     * object stream take array of objects, stream, apply methods to it
     * map person domain to people per station view
     */

}
