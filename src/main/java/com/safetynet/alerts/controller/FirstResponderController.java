package com.safetynet.alerts.controller;
import com.safetynet.alerts.view.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.safetynet.alerts.service.FirstResponderService;

import java.util.List;
import java.util.Map;

/**
 * Controller for the FirstResponder class
 */
@RestController
@Slf4j
public class FirstResponderController {
    private final FirstResponderService firstResponderService;

    /**
     * Constructor for the first responder controller
     * @param firstResponderService
     */
    public FirstResponderController(FirstResponderService firstResponderService) {
        this.firstResponderService = firstResponderService;
    }

    /**
     * Gets all of the people for a corresponding firestation when the station number is provided.
     * This method also provides the count of adults and count of children for that specific station
     * @param stationNumber
     * @return
     */
    @GetMapping("/firestation")
    public PeoplePerStation getPeopleByStation(@RequestParam String stationNumber) {
        log.info("Searching for people by station:", stationNumber);
        //create local field in the method for the result and then return the result
        return firstResponderService.getPeopleByStation(stationNumber);
    }

    /**
     * Returns the list of children by address and includes info such as
     * their first and last name, age, and list of people who also live
     * at that household
     * @param address
     * @return
     */
    @GetMapping("/childAlert")
    public ChildrenByAddress getChildrenByAddress(@RequestParam String address) {
        log.info("Searching for children by address {}", address);
        return firstResponderService.getChildrenByAddress(address);
    }

    /**
     * Returns the list of phone numbers that are serviced by a specific fire station
     * @param firestation
     * @return
     */
    @GetMapping("/phoneAlert")
    public PhonesPerStation getPhonesPerStation(@RequestParam(name = "firestation") String firestation) {
        return firstResponderService.getPhonesPerStation(firestation);
    }


    /**
     * Returns the list of residents liviing at the provivded address.
     * Also gives the station number that serves that address.
     * Each person has information such as name, number, age, and medical history
      * @param address
     * @return
     */
    @GetMapping("/fire")
    public ResidentsPerAddress getResidentsPerAddress(@RequestParam String address){
        return firstResponderService.getResidentsPerAddress(address);
    }

    @GetMapping("/flood")
    public List<HouseholdsByStation> getHouseholdsByFirestation(@RequestParam List<String> stations){
        return firstResponderService.getHouseholdsByFirestation(stations);
    }

}