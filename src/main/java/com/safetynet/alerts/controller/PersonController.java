package com.safetynet.alerts.controller;
//add logs
import com.safetynet.alerts.domain.Person;
import com.safetynet.alerts.repository.DataParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.safetynet.alerts.service.PersonService;
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
     * Gets all the people
     *
     * @return
     */
    @GetMapping
    public List<Person> getPeople() {
        log.info("Getting people...");
        return personService.findAll();
    }

    /**
     * Get one person by their name
     *
     * @param lastName
     * @param firstName
     * @return
     */
    @GetMapping("/{lastName}/{firstName}")
    public ResponseEntity<Person> getPerson(
            @PathVariable String lastName,
            @PathVariable String firstName) {
        return personService.findByName(lastName, firstName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Add a new person
     *
     * @param newPerson
     * @return
     */
    @PostMapping
    public ResponseEntity<Person> postPerson(@RequestBody Person newPerson) {
        Person savedPerson = personService.postPerson(newPerson);
        return new ResponseEntity<>(savedPerson, HttpStatus.CREATED);
    }


    /**
     * Update an existing person, not changing the name
     *
     * @param lastName
     * @param firstName
     * @param updates
     * @return
     */
    @PutMapping(path = "/{lastName}/{firstName}")
    public ResponseEntity<?> putPerson(
            @PathVariable String lastName,
            @PathVariable String firstName,
            @RequestBody Person updates) {
        try {
            return personService.updatePerson(lastName, firstName, updates)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Person not found: " + firstName  + " " + lastName));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Delete a person mapping by their name
     *
     * @param lastName
     * @param firstName
     * @return
     */
    @DeleteMapping("/{lastName}/{firstName}")
    public ResponseEntity<Void> delete(
            @PathVariable String lastName,
            @PathVariable String firstName) {

        boolean deleted = personService.deleteByName(lastName, firstName);
        return deleted ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}