package com.safetynet.alerts;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.service.MedicalRecordService;
import com.safetynet.alerts.controller.MedicalRecordController;
import com.safetynet.alerts.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for MedicalRecordController using standalone MockMvc.
 */
class MedicalRecordControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private MedicalRecordService medicalRecordService;
    private PersonService personService;

    @BeforeEach
    void setUp() {
        medicalRecordService = mock(MedicalRecordService.class);
        personService = mock(PersonService.class);
        MedicalRecordController controller = new MedicalRecordController(medicalRecordService, personService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private MedicalRecord mr(String first, String last, String birthdate, List<String> meds, List<String> allergies) {
        MedicalRecord m = new MedicalRecord();
        m.setFirstName(first);
        m.setLastName(last);
        m.setBirthdate(birthdate);
        m.setMedications(meds);
        m.setAllergies(allergies);
        return m;
    }

    // -------------------------
    // GET /medicalRecords
    // -------------------------
    @Test
    @DisplayName("GET /medicalRecords returns list of records with 200 OK")
    void getAllMedicalRecords_returnsList() throws Exception {
        List<MedicalRecord> list = Arrays.asList(
                mr("John", "Boyd", "03/06/1984",
                        Arrays.asList("aznol:350mg", "hydrapermazol:100mg"),
                        Arrays.asList("nillacilan")),
                mr("Jacob", "Boyd", "03/06/1989",
                        Arrays.asList("pharmacol:5000mg"),
                        Arrays.asList("peanut"))
        );
        when(medicalRecordService.findAll()).thenReturn(list);

        mockMvc.perform(get("/medicalRecords"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].lastName", is("Boyd")))
                .andExpect(jsonPath("$[0].birthdate", is("03/06/1984")))
                .andExpect(jsonPath("$[0].medications", hasSize(2)))
                .andExpect(jsonPath("$[0].allergies", hasSize(1)));

        verify(medicalRecordService).findAll();
        verifyNoMoreInteractions(medicalRecordService);
    }

    /**
     * Test adding a medical record via POST
     *
     * @throws Exception
     */
    @Test
    @DisplayName("POST /medicalRecords returns 201 Created with saved record")
    void postMedicalRecord_created() throws Exception {
        MedicalRecord payload = mr("John", "Boyd", "03/06/1984",
                Arrays.asList("aznol:350mg", "hydrapermazol:100mg"),
                Arrays.asList("nillacilan"));

        when(medicalRecordService.postMedicalRecord(any(MedicalRecord.class))).thenReturn(payload);

        mockMvc.perform(post("/medicalRecords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Boyd")))
                .andExpect(jsonPath("$.birthdate", is("03/06/1984")))
                .andExpect(jsonPath("$.medications", hasSize(2)))
                .andExpect(jsonPath("$.allergies[0]", is("nillacilan")));

        verify(medicalRecordService).postMedicalRecord(any(MedicalRecord.class));
    }

    /**
     * Test udpate medical record via PUT
     */
    @Nested
    @DisplayName("PUT /medicalRecords/{lastName}/{firstName}")
    class UpdateMedicalRecordTests {

        @Test
        @DisplayName("returns 200 OK and updated record when found")
        void put_ok() throws Exception {
            String last = "Boyd";
            String first = "John";

            MedicalRecord updates = new MedicalRecord();
            updates.setBirthdate("01/01/2000");
            updates.setMedications(Arrays.asList("newmed:500mg"));
            updates.setAllergies(Arrays.asList("peanut"));

            MedicalRecord updated = mr(first, last, "01/01/2000",
                    Arrays.asList("newmed:500mg"),
                    Arrays.asList("peanut"));

            when(medicalRecordService.updateMedicalRecord(eq(last), eq(first), any(MedicalRecord.class)))
                    .thenReturn(Optional.of(updated));

            mockMvc.perform(put("/medicalRecords/{lastName}/{firstName}", last, first)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updates)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.firstName", is(first)))
                    .andExpect(jsonPath("$.lastName", is(last)))
                    .andExpect(jsonPath("$.birthdate", is("01/01/2000")))
                    .andExpect(jsonPath("$.medications[0]", is("newmed:500mg")))
                    .andExpect(jsonPath("$.allergies[0]", is("peanut")));

            verify(medicalRecordService).updateMedicalRecord(eq(last), eq(first), any(MedicalRecord.class));
        }

        @Test
        @DisplayName("returns 404 Not Found when person record is missing")
        void put_notFound() throws Exception {
            String last = "Unknown";
            String first = "Ghost";

            when(medicalRecordService.updateMedicalRecord(eq(last), eq(first), any(MedicalRecord.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(put("/medicalRecords/{lastName}/{firstName}", last, first)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new MedicalRecord())))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("person not found: " + last + first)));

            verify(medicalRecordService).updateMedicalRecord(eq(last), eq(first), any(MedicalRecord.class));
        }

        @Test
        @DisplayName("returns 400 Bad Request when service throws IllegalArgumentException")
        void put_badRequest_serviceThrows() throws Exception {
            String last = "Boyd";
            String first = "John";

            when(medicalRecordService.updateMedicalRecord(eq(last), eq(first), any(MedicalRecord.class)))
                    .thenThrow(new IllegalArgumentException("Invalid medical record data"));

            mockMvc.perform(put("/medicalRecords/{lastName}/{firstName}", last, first)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new MedicalRecord())))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Invalid medical record data")));

            verify(medicalRecordService).updateMedicalRecord(eq(last), eq(first), any(MedicalRecord.class));
        }
    }

    /**
     * Test delete a medical record
     */
    @Nested
    @DisplayName("DELETE /medicalRecords/{lastName}/{firstName}")
    class DeleteMedicalRecordTests {

        @Test
        @DisplayName("returns 200 OK when deletion succeeds")
        void delete_ok() throws Exception {
            when(medicalRecordService.deleteByName("Boyd", "John")).thenReturn(true);

            mockMvc.perform(delete("/medicalRecords/{lastName}/{firstName}", "Boyd", "John"))
                    .andExpect(status().isOk());

            verify(medicalRecordService).deleteByName("Boyd", "John");
        }

        @Test
        @DisplayName("returns 404 Not Found when record does not exist")
        void delete_notFound() throws Exception {
            when(medicalRecordService.deleteByName("Unknown", "Ghost")).thenReturn(false);

            mockMvc.perform(delete("/medicalRecords/{lastName}/{firstName}", "Unknown", "Ghost"))
                    .andExpect(status().isNotFound());

            verify(medicalRecordService).deleteByName("Unknown", "Ghost");
        }
    }
}
