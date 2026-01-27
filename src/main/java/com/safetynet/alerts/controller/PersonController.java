package com.safetynet.alerts.controller;

import com.safetynet.alerts.domain.Person;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class PersonController {
    @GetMapping
    public List<Person> getPeople() {
        List<Person> people= new ArrayList<>();
        people.add(new Person());
        return people;
    }
}
//using jackson to parse json
//add paths