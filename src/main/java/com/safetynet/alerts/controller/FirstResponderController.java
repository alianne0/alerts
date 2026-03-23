package com.safetynet.alerts.controller;

import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.service.FirstResponderService;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.view.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for the FirstResponder class
 */
@RestController
@Slf4j
public class FirstResponderController {

    private final DataParser data;
    private final FirstResponderService firstResponderService;

    /**
     * Constructor for the first responder controller
     *
     * @param firstResponderService
     */
    public FirstResponderController(FirstResponderService firstResponderService, DataParser data) {
        this.firstResponderService = firstResponderService;
        this.data = data;
    }

    /**
     * Gets all of the people for a corresponding firestation
     *
     * @param stationNumber
     * @return
     */
    @GetMapping("/firestation")
    public PeoplePerStation getPeopleByStation(@RequestParam String stationNumber) {
        log.info("GET /firestation - Retrieving people for station {}", stationNumber);

        try {
            PeoplePerStation result =
                    firstResponderService.getPeopleByStation(stationNumber);

            log.info("Successfully retrieved people for station {}", stationNumber);
            return result;

        } catch (Exception ex) {
            log.error("Error retrieving people for station {}", stationNumber, ex);
            throw ex;
        }
    }

    /**
     * Returns the list of children by address
     *
     * @param address
     * @return
     */
    @GetMapping("/childAlert")
    public ChildrenByAddress getChildrenByAddress(@RequestParam String address) {
        log.info("GET /childAlert - Retrieving children for address {}", address);

        try {
            ChildrenByAddress result =
                    firstResponderService.getChildrenByAddress(address);

            log.info("Child alert lookup completed for address {}", address);
            return result;

        } catch (Exception ex) {
            log.error("Error retrieving children for address {}", address, ex);
            throw ex;
        }
    }

    /**
     * Returns the list of phone numbers serviced by a fire station
     *
     * @param firestation
     * @return
     */
    @GetMapping("/phoneAlert")
    public PhonesPerStation getPhonesPerStation(
            @RequestParam(name = "firestation") String firestation) {

        log.info("GET /phoneAlert - Retrieving phone numbers for station {}", firestation);

        try {
            PhonesPerStation result =
                    firstResponderService.getPhonesPerStation(firestation);

            log.info("Phone alert lookup completed for station {}", firestation);
            return result;

        } catch (Exception ex) {
            log.error("Error retrieving phone numbers for station {}", firestation, ex);
            throw ex;
        }
    }

    /**
     * Returns the list of residents living at the provided address
     *
     * @param address
     * @return
     */
    @GetMapping("/fire")
    public ResidentsPerAddress getResidentsPerAddress(@RequestParam String address) {
        log.info("GET /fire - Retrieving residents for address {}", address);

        try {
            ResidentsPerAddress result =
                    firstResponderService.getResidentsPerAddress(address);

            log.info("Successfully retrieved residents for address {}", address);
            return result;

        } catch (Exception ex) {
            log.error("Error retrieving residents for address {}", address, ex);
            throw ex;
        }
    }

    /**
     * Returns households served by firestations
     *
     * @param stations
     * @return
     */
    @GetMapping("/flood")
    public List<HouseholdsByStation> getHouseholdsByFirestation(
            @RequestParam List<String> stations) {

        log.info("GET /flood - Retrieving households for station(s) {}", stations);

        try {
            List<HouseholdsByStation> result =
                    firstResponderService.getHouseholdsByFirestation(stations);

            log.info("Successfully retrieved households for station(s) {}", (stations));
            return result;

        } catch (Exception ex) {
            log.error("Error retrieving households for station(s) {}", stations, ex);
            throw ex;
        }
    }

    /**
     * Returns person information by last name
     *
     * @param lastName
     * @return
     */
    @GetMapping("/personInfo")
    public PersonInfo getPersonInfo(@RequestParam String lastName) {
        log.info("GET /personInfo - Retrieving person info for lastName {}", lastName);

        try {
            PersonInfo result = firstResponderService.getPersonInfo(lastName);

            log.info("Successfully retrieved person info for lastName {}", lastName);
            return result;

        } catch (Exception ex) {
            log.error("Error retrieving person info for lastName {}", lastName, ex);
            throw ex;
        }
    }

    /**
     * Returns resident emails by city
     *
     * @param city
     * @return
     */
    @GetMapping("/communityEmail")
    public ResidentEmails getResidentEmails(@RequestParam String city) {
        log.info("GET /communityEmail - Retrieving resident emails for city {}", city);

        try {
            ResidentEmails result =
                    firstResponderService.getResidentEmails(city);

            log.info("Successfully retrieved resident emails for city {}", city);
            return result;

        } catch (Exception ex) {
            log.error("Error retrieving resident emails for city {}", city, ex);
            throw ex;
        }
    }
}
