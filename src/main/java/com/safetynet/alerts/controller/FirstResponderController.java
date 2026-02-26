package com.safetynet.alerts.controller;
import com.safetynet.alerts.view.PeoplePerStation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.safetynet.alerts.service.FirstResponderService;

@RestController
@Slf4j
public class FirstResponderController {
    private final FirstResponderService firstResponderService;

    public FirstResponderController(FirstResponderService firstResponderService) {

        this.firstResponderService = firstResponderService;
    }

    //ad query param to the parameter
//make sure this endpoint works
    @GetMapping("/firestation")
    public PeoplePerStation getPeopleByStation(@RequestParam String stationNumber) {
        log.info("Searching for the people by station for number:", stationNumber);
        return firstResponderService.getPeopleByStation(stationNumber);

        //return firstResponderService.getPeopleByStation(stationNumber);
        // pass to first responder service, which will find all info for view object (peoplePerStation) back to controller
    }
}