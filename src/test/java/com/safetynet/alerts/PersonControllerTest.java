package com.safetynet.alerts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.controller.PersonController;
import com.safetynet.alerts.domain.Person;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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
 * Unit tests for PersonController using standalone MockMvc.
 */
class PersonControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private PersonService personService;
    private DataParser dataParser;

    @BeforeEach
    void setUp() {
        personService = mock(PersonService.class);
        dataParser = mock(DataParser.class);
        PersonController controller = new PersonController(dataParser, personService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

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
     * Test get all persons
     *
     * @throws Exception
     */
    @Test
    @DisplayName("GET /persons returns list of persons with 200")
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
     * Test get a single person
     *
     * @throws Exception
     */
    @Test
    @DisplayName("GET /persons/{lastName}/{firstName} returns person with 200")
    void getPerson_found() throws Exception {
        Person p = samplePerson();
        when(personService.findByName("Doe", "John")).thenReturn(Optional.of(p));

        mockMvc.perform(get("/persons/Doe/John"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));

        verify(personService).findByName("Doe", "John");
    }

    @Test
    @DisplayName("GET /persons/{lastName}/{firstName} returns 404 when person not found")
    void getPerson_notFound() throws Exception {
        when(personService.findByName("Doe", "Jane")).thenReturn(Optional.empty());

        mockMvc.perform(get("/persons/Doe/Jane"))
                .andExpect(status().isNotFound());

        verify(personService).findByName("Doe", "Jane");
    }

    /**
     * Test adding a new person via POST
     *
     * @throws Exception
     */
    @Test
    @DisplayName("POST /persons returns 201 Created with saved person")
    void postPerson_created() throws Exception {
        Person toSave = samplePerson();
        Person saved = samplePerson();
        when(personService.postPerson(ArgumentMatchers.any(Person.class))).thenReturn(saved);

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
     * Test updating a person via PUT
     */
    @Nested
    @DisplayName("PUT /persons/{lastName}/{firstName}")
    class PutPersonTests {

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
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.address", is("New Address")))
                    .andExpect(jsonPath("$.city", is("New City")))
                    .andExpect(jsonPath("$.phone", is("999-999-9999")));

            verify(personService).updatePerson(eq("Doe"), eq("John"), any(Person.class));
        }

        @Test
        @DisplayName("returns 404 when target person not found")
        void putPerson_notFound() throws Exception {
            Person updates = new Person();
            updates.setAddress("Nowhere 1");

            when(personService.updatePerson(eq("Doe"), eq("Ghost"), any(Person.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(put("/persons/Doe/Ghost")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updates)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Person not found: Ghost Doe")));

            verify(personService).updatePerson(eq("Doe"), eq("Ghost"), any(Person.class));
        }

        @Test
        @DisplayName("returns 400 when service throws IllegalArgumentException")
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
     * Test deleting a person
     */
    @Nested
    @DisplayName("DELETE /persons/{lastName}/{firstName}")
    class DeletePersonTests {
        @Test
        @DisplayName("returns 200 when deletion succeeds")
        void delete_ok() throws Exception {
            when(personService.deleteByName("Doe", "John")).thenReturn(true);

            mockMvc.perform(delete("/persons/Doe/John"))
                    .andExpect(status().isOk());

            verify(personService).deleteByName("Doe", "John");
        }

        @Test
        @DisplayName("returns 404 when person not found")
        void delete_notFound() throws Exception {
            when(personService.deleteByName("Doe", "Ghost")).thenReturn(false);

            mockMvc.perform(delete("/persons/Doe/Ghost"))
                    .andExpect(status().isNotFound());

            verify(personService).deleteByName("Doe", "Ghost");
        }
    }
}