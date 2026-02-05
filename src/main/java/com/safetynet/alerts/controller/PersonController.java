package com.safetynet.alerts.controller;

import com.safetynet.alerts.domain.Person;
import com.safetynet.alerts.repository.DataParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.safetynet.alerts.service.PersonService;
import java.util.List;

@RestController
@RequestMapping("/person")
public class PersonController {

    private final DataParser data;
    private final PersonService personService;

    @Autowired
    public PersonController(DataParser data, PersonService personService) {
        this.data = data;
        this.personService = personService;
    }

    // Get all people
    @GetMapping
    public List<Person> getPeople() {
        return personService.findAll();
    }

    // Get one person
    @GetMapping("/{firstName}/{lastName}")
    public ResponseEntity<Person> getPerson(
            @PathVariable String firstName,
            @PathVariable String lastName) {
        return personService.findByName(firstName, lastName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Post a person
    @PostMapping
    public ResponseEntity<Person> postPerson(@RequestBody Person newPerson) {
        Person savedPerson = personService.postPerson(newPerson);
        return new ResponseEntity<>(savedPerson, HttpStatus.CREATED);
    }

    // update an existing person
    @PatchMapping("/{firstName}/{lastName}")
    public ResponseEntity<?> patchPerson(
            @PathVariable String firstName,
            @PathVariable String lastName,
            @RequestBody Person updates) {
        try {
            return personService.updatePerson(firstName, lastName, updates)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Person not found: " + firstName + " " + lastName));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Delete a person
    @DeleteMapping("/{firstName}/{lastName}")
    public ResponseEntity<Void> delete(
            @PathVariable String firstName,
            @PathVariable String lastName) {

        boolean deleted = personService.deleteByName(lastName, firstName);
        return deleted ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}