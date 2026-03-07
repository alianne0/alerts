package com.safetynet.alerts.controller;
import com.safetynet.alerts.view.PeoplePerStation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.safetynet.alerts.service.FirstResponderService;

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
}