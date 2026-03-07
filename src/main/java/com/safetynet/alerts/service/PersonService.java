package com.safetynet.alerts.service;

import com.safetynet.alerts.controller.PersonController;
import com.safetynet.alerts.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.safetynet.alerts.repository.DataParser;

import java.util.*;

/**
 * Service class for the person
 */
@Service
public class PersonService {

    private final DataParser data;

    /**
     * Constructor for the person service
     * @param data
     */
    @Autowired
    public PersonService(DataParser data) {
        this.data = data;
    }

    /**
     * Returns all of the mappings in Person
     * @return
     */
    public List<Person> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(data.getPersons()));
    }

    /**
     * Returns a person mapping and all of their information given their first and last name
     * @param lastName
     * @param firstName
     * @return
     */
    public Optional<Person> findByName(String lastName, String firstName) {
        return data.getPersons().stream()
                .filter(p -> equalsTrimmed(p.getLastName(), lastName)
                        && equalsTrimmed(p.getFirstName(), firstName))
                .findFirst( );
    }

    /**
     * Posts a new person mapping
     * @param person
     * @return
     */
    public Person postPerson(Person person) {
        Objects.requireNonNull(person, "person must not be null");
        List<Person> persons = data.getPersons();

        for (int i = 0; i < persons.size(); i++) {
            Person existing = persons.get(i);
            if (equalsTrimmed(existing.getLastName(), person.getLastName())
                    && equalsTrimmed(existing.getFirstName(), person.getFirstName())) {
                existing.setFirstName(person.getFirstName());
                existing.setLastName(person.getLastName());
                existing.setAddress(person.getAddress());
                existing.setCity(person.getCity());
                existing.setZip(person.getZip());
                existing.setPhone(person.getPhone());
                existing.setEmail(person.getEmail());
                return existing;
            }
        }
        persons.add(person);
        return person;
    }

    /**
     * Deletes a person mapping given their first and last name
     * @param lastName
     * @param firstName
     * @return
     */
    public boolean deleteByName(String lastName, String firstName) {
        return data.getPersons().removeIf(
                p -> equalsTrimmed(p.getLastName(), lastName)
                        && equalsTrimmed(p.getFirstName(), firstName)
        );
    }

    /**
     * Updates a person mapping given their first and last name
     * @param lastName
     * @param firstName
     * @param updates
     * @return
     */
    public Optional<Person> updatePerson(String lastName, String firstName, Person updates) {
        Objects.requireNonNull(updates, "updates cannot be null");
        if (updates.getLastName() != null && !equalsTrimmed(updates.getLastName(), lastName)) {
            throw new IllegalArgumentException("last name cannot be changed");
        }
        if (updates.getFirstName() != null && !equalsTrimmed(updates.getFirstName(), firstName)){
            throw new IllegalArgumentException("first name cannot be changed");
        }
        for (Person existing : data.getPersons()) {
            if (equalsTrimmed(existing.getLastName(), lastName) && equalsTrimmed(existing.getFirstName(), firstName)) {
                if (updates.getAddress() != null) {
                    existing.setAddress(updates.getAddress());
                }
                if (updates.getCity() != null) {
                    existing.setCity(updates.getCity());
                }
                if(updates.getZip() != null) {
                    existing.setZip(updates.getZip());
                }
                if(updates.getPhone() != null) {
                    existing.setPhone(updates.getPhone());
                }
                if (updates.getEmail() != null) {
                    existing.setEmail(updates.getEmail());
                }
                return Optional.of(existing);
            }
        }
        return Optional.empty();
    }

    private boolean equalsTrimmed(String a, String b) {
        String aa = (a == null) ? null : a.trim();
        String bb = (b == null) ? null : b.trim();
        return Objects.equals(aa, bb);
    }
}