package com.safetynet.alerts.service;

import com.safetynet.alerts.controller.PersonController;
import com.safetynet.alerts.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.safetynet.alerts.repository.DataParser;

import java.util.*;

@Service
public class PersonService {

    private final DataParser data;

    @Autowired
    public PersonService(DataParser data) {
        this.data = data;
    }

    public List<Person> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(data.getPersons()));
    }

    public Optional<Person> findByName(String firstName, String lastName) {
        return data.getPersons().stream()
                .filter(p -> equalsTrimmed(p.getFirstName(), firstName)
                        && equalsTrimmed(p.getLastName(), lastName))
                .findFirst( );
    }

    public Person postPerson(Person person) {
        Objects.requireNonNull(person, "person must not be null");
        List<Person> persons = data.getPersons();

        for (int i = 0; i < persons.size(); i++) {
            Person existing = persons.get(i);
            if (equalsTrimmed(existing.getLastName(), person.getLastName())
                    && equalsTrimmed(existing.getFirstName(), person.getFirstName())) {
                persons.set(i, person);
                return person;
            }
        }
        persons.add(person);
        return person;
    }

    public boolean deleteByName(String lastName, String firstName) {
        return data.getPersons().removeIf(
                p -> equalsTrimmed(p.getLastName(), lastName)
                        && equalsTrimmed(p.getFirstName(), firstName)
        );
    }
    //update a person
    public Optional<Person> updatePerson(String firstName, String lastName, Person updates) {
        Objects.requireNonNull(updates, "updates cannot be null");
        if (updates.getFirstName() != null && !equalsTrimmed(updates.getFirstName(), firstName)) {
            throw new IllegalArgumentException("firstname cannot be changed");
        }
        if (updates.getLastName() != null && !equalsTrimmed(updates.getLastName(), lastName)){
            throw new IllegalArgumentException("last name cannot be changed");
        }
        for (Person existing : data.getPersons()) {
            if (equalsTrimmed(existing.getFirstName(), firstName) && equalsTrimmed(existing.getLastName(), lastName)) {
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
                    existing.setZip(updates.getPhone());
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