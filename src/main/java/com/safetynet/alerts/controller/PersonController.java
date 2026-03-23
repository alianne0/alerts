package com.safetynet.alerts.controller;

import com.safetynet.alerts.domain.Person;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for Person
 */
@RestController
@RequestMapping("/persons")
@Slf4j
public class PersonController {

    private final DataParser data;
    private final PersonService personService;

    /**
     * Constructor for the Person Controller
     *
     * @param data
     * @param personService
     */
    @Autowired
    public PersonController(DataParser data, PersonService personService) {
        this.data = data;
        this.personService = personService;
    }

    /**
     * Get all the people
     *
     * @return list of people
     */
    @GetMapping
    public List<Person> getPeople() {
        log.info("GET /persons - Retrieving all persons");

        List<Person> people = personService.findAll();
        log.info("Successfully retrieved {} persons", people.size());

        return people;
    }

    /**
     * Get one person by their name
     *
     * @param lastName
     * @param firstName
     * @return person if found
     */
    @GetMapping("/{lastName}/{firstName}")
    public ResponseEntity<Person> getPerson(
            @PathVariable String lastName,
            @PathVariable String firstName) {

        log.info("GET /persons/{}/{} - Retrieving person", lastName, firstName);

        return personService.findByName(lastName, firstName)
                .map(person -> {
                    log.info("Person found: {} {}", firstName, lastName);
                    return ResponseEntity.ok(person);
                })
                .orElseGet(() -> {
                    log.warn("Person not found: {} {}", firstName, lastName);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Add a new person
     *
     * @param newPerson
     * @return created person
     */
    @PostMapping
    public ResponseEntity<Person> postPerson(@RequestBody Person newPerson) {

        log.info("POST /persons - Creating person {} {}",
                newPerson.getFirstName(),
                newPerson.getLastName());

        try {
            Person savedPerson = personService.postPerson(newPerson);
            if (data != null) {
                data.saveToFile();
            }

            log.info("Person successfully created: {} {}",
                    savedPerson.getFirstName(),
                    savedPerson.getLastName());

            return new ResponseEntity<>(savedPerson, HttpStatus.CREATED);

        } catch (Exception ex) {
            log.error("Error while creating person {} {}",
                    newPerson.getFirstName(),
                    newPerson.getLastName(),
                    ex);
            throw ex;
        }
    }

    /**
     * Update an existing person, not changing the name
     *
     * @param lastName
     * @param firstName
     * @param updates
     * @return updated person or error
     */
    @PutMapping("/{lastName}/{firstName}")
    public ResponseEntity<?> putPerson(
            @PathVariable String lastName,
            @PathVariable String firstName,
            @RequestBody Person updates) {

        log.info("PUT /persons/{}/{} - Updating person", lastName, firstName);

        try {
            return personService.updatePerson(lastName, firstName, updates)
                    .<ResponseEntity<?>>map(updated -> {
                        if (data != null) {
                            data.saveToFile();
                        }
                        log.info("Person updated successfully: {} {}", firstName, lastName);
                        return ResponseEntity.ok(updated);
                    })
                    .orElseGet(() -> {
                        log.warn("Person not found for update: {} {}", firstName, lastName);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Person not found: " + firstName + " " + lastName);
                    });

        } catch (IllegalArgumentException ex) {
            log.error("Invalid update request for {} {}: {}",
                    firstName, lastName, ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Delete a person mapping by their name
     *
     * @param lastName
     * @param firstName
     * @return response status
     */
    @DeleteMapping("/{lastName}/{firstName}")
    public ResponseEntity<Void> delete(
            @PathVariable String lastName,
            @PathVariable String firstName) {

        log.info("DELETE /persons/{}/{} - Deleting person", lastName, firstName);

        try {
            boolean deleted = personService.deleteByName(lastName, firstName);

            if (deleted) {
                if (data != null) {
                    data.saveToFile();
                }
                log.info("Person deleted successfully: {} {}", firstName, lastName);
                return ResponseEntity.ok().build();
            } else {
                log.warn("Person not found for deletion: {} {}", firstName, lastName);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception ex) {
            log.error("Error while deleting person {} {}",
                    firstName, lastName, ex);
            throw ex;
        }
    }
}