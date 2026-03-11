package com.safetynet.alerts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.domain.Person;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the Person controller
 */
@ExtendWith(MockitoExtension.class)
class PersonControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private PersonService personService;
    private DataParser dataParser;

    /**
     * Initializes the controller with mocked dependencies before each test.
     */
    @BeforeEach
    void setUp() {
        personService = mock(PersonService.class);
        dataParser = mock(DataParser.class);

        PersonController controller =
                new PersonController(dataParser, personService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Creates a fully populated {@link Person} for use in tests.
     */
    private Person samplePerson() {
        Person p = new Person();
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setAddress("1509 Culver St");
        p.setCity("Culver");
        p.setZip("97451");
        p.setPhone("841-874-6512");
        p.setEmail("john.doe@example.com");
        return p;
    }

    /**
     * Test that GET all persons are returned successfully.
     */
    @Test
    @DisplayName("GET /persons returns a list of persons")
    void getPeople_returnsList() throws Exception {
        List<Person> list = Arrays.asList(samplePerson(), samplePerson());
        when(personService.findAll()).thenReturn(list);

        mockMvc.perform(get("/persons"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].lastName", is("Doe")));

        verify(personService).findAll();
        verifyNoMoreInteractions(personService);
    }

    /**
     * Test using GET to get a single person
     */
    @Test
    @DisplayName("GET /persons/{lastName}/{firstName} returns person when found")
    void getPerson_found() throws Exception {
        when(personService.findByName("Doe", "John"))
                .thenReturn(Optional.of(samplePerson()));

        mockMvc.perform(get("/persons/Doe/John"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));

        verify(personService).findByName("Doe", "John");
    }

    /**
     * Tests that requesting an unknown person returns 404
     */
    @Test
    @DisplayName("GET /persons/{lastName}/{firstName} returns 404 when not found")
    void getPerson_notFound() throws Exception {
        when(personService.findByName("Doe", "Jane"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/persons/Doe/Jane"))
                .andExpect(status().isNotFound());

        verify(personService).findByName("Doe", "Jane");
    }

    /**
     * Test that a new person can be created via POST
     */
    @Test
    @DisplayName("POST /persons creates a new person")
    void postPerson_created() throws Exception {
        Person toSave = samplePerson();

        when(personService.postPerson(any(Person.class)))
                .thenReturn(samplePerson());

        mockMvc.perform(post("/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toSave)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));

        verify(personService).postPerson(any(Person.class));
    }

    /**
     * Tests for updating an existing person via PUT
     */
    @Nested
    @DisplayName("PUT /persons/{lastName}/{firstName}")
    class PutPersonTests {

        /**
         * Test that an existing person can be updated
         */
        @Test
        @DisplayName("returns 200 and updated person when found")
        void putPerson_ok() throws Exception {
            Person updates = new Person();
            updates.setAddress("New Address");
            updates.setCity("New City");
            updates.setPhone("999-999-9999");

            Person updated = samplePerson();
            updated.setAddress(updates.getAddress());
            updated.setCity(updates.getCity());
            updated.setPhone(updates.getPhone());

            when(personService.updatePerson(eq("Doe"), eq("John"), any(Person.class)))
                    .thenReturn(Optional.of(updated));

            mockMvc.perform(put("/persons/Doe/John")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updates)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.address", is("New Address")))
                    .andExpect(jsonPath("$.city", is("New City")))
                    .andExpect(jsonPath("$.phone", is("999-999-9999")));

            verify(personService).updatePerson(eq("Doe"), eq("John"), any(Person.class));
        }

        /**
         * Test that updating a non-existent person returns 404
         */
        @Test
        @DisplayName("returns 404 when target person does not exist")
        void putPerson_notFound() throws Exception {
            when(personService.updatePerson(eq("Doe"), eq("Ghost"), any(Person.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(put("/persons/Doe/Ghost")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new Person())))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Person not found: Ghost Doe")));

            verify(personService).updatePerson(eq("Doe"), eq("Ghost"), any(Person.class));
        }

        /**
         * Test that invalid updates are rejected with a 400 response
         */
        @Test
        @DisplayName("returns 400 when update violates business rules")
        void putPerson_badRequest() throws Exception {
            Person updates = new Person();
            updates.setFirstName("ShouldNotChange");

            when(personService.updatePerson(eq("Doe"), eq("John"), any(Person.class)))
                    .thenThrow(new IllegalArgumentException("Name cannot be changed"));

            mockMvc.perform(put("/persons/Doe/John")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updates)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Name cannot be changed")));

            verify(personService).updatePerson(eq("Doe"), eq("John"), any(Person.class));
        }
    }

    /**
     * Tests for deleting persons by name via DELETE
     */
    @Nested
    @DisplayName("DELETE /persons/{lastName}/{firstName}")
    class DeletePersonTests {

        /**
         * Test that deleting an existing person succeeds
         */
        @Test
        @DisplayName("returns 200 when deletion succeeds")
        void delete_ok() throws Exception {
            when(personService.deleteByName("Doe", "John"))
                    .thenReturn(true);

            mockMvc.perform(delete("/persons/Doe/John"))
                    .andExpect(status().isOk());

            verify(personService).deleteByName("Doe", "John");
        }

        /**
         * Test that deleting a non-existent person returns 404
         */
        @Test
        @DisplayName("returns 404 when person not found")
        void delete_notFound() throws Exception {
            when(personService.deleteByName("Doe", "Ghost"))
                    .thenReturn(false);

            mockMvc.perform(delete("/persons/Doe/Ghost"))
                    .andExpect(status().isNotFound());

            verify(personService).deleteByName("Doe", "Ghost");
        }
    }
}
