package com.safetynet.alerts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.service.MedicalRecordService;
import com.safetynet.alerts.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-level tests for the MedicalRecordController class
 */
@ExtendWith(MockitoExtension.class)
class MedicalRecordControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    @Mock
    private MedicalRecordService medicalRecordService;

    @Mock
    private PersonService personService;

    @InjectMocks
    private MedicalRecordController medicalRecordController;

    /**
     * Sets up standalone MockMvc with the controller under test.
     * No Spring context is started.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(medicalRecordController)
                .build();
    }

    /**
     * Test that GET all medical records returns HTTP 200 OK.
     */
    @Test
    @DisplayName("GET /medicalRecords returns 200 OK")
    void getAllMedicalRecords_returnsOk() throws Exception {
        when(medicalRecordService.findAll())
                .thenReturn(List.of(Mockito.mock(MedicalRecord.class)));

        mockMvc.perform(get("/medicalRecords"))
                .andExpect(status().isOk());
    }

    /**
     * Test that creating a medical record via POST returns HTTP 201 Created.
     */
    @Test
    @DisplayName("POST /medicalRecords creates a new medical record")
    void postMedicalRecord_returnsCreated() throws Exception {
        MedicalRecord input = Mockito.mock(MedicalRecord.class);
        MedicalRecord saved = Mockito.mock(MedicalRecord.class);

        when(medicalRecordService.postMedicalRecord(any()))
                .thenReturn(saved);

        mockMvc.perform(post("/medicalRecords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated());
    }

    /**
     * Test that updating an existing medical record via PUT succeeds.
     */
    @Test
    @DisplayName("PUT /medicalRecords/{lastName}/{firstName} returns 200 when record exists")
    void updateMedicalRecord_whenFound_returnsOk() throws Exception {
        MedicalRecord updated = Mockito.mock(MedicalRecord.class);

        when(medicalRecordService.updateMedicalRecord(
                eq("Doe"),
                eq("John"),
                any(MedicalRecord.class)))
                .thenReturn(Optional.of(updated));

        mockMvc.perform(put("/medicalRecords/Doe/John")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    /**
     * Test that updating a non-existent medical record returns 404.
     */
    @Test
    @DisplayName("PUT /medicalRecords/{lastName}/{firstName} returns 404 when record not found")
    void updateMedicalRecord_whenNotFound_returns404() throws Exception {
        when(medicalRecordService.updateMedicalRecord(
                eq("Doe"),
                eq("John"),
                any(MedicalRecord.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/medicalRecords/Doe/John")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test that invalid update requests are translated to HTTP 400.
     */
    @Test
    @DisplayName("PUT /medicalRecords/{lastName}/{firstName} returns 400 on invalid update")
    void updateMedicalRecord_whenIllegalArgument_returns400() throws Exception {
        when(medicalRecordService.updateMedicalRecord(
                eq("Doe"),
                eq("John"),
                any(MedicalRecord.class)))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        mockMvc.perform(put("/medicalRecords/Doe/John")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test that DELETE an existing medical record succeeds.
     */
    @Test
    @DisplayName("DELETE /medicalRecords/{lastName}/{firstName} returns 200 when deleted")
    void deleteMedicalRecord_whenDeleted_returnsOk() throws Exception {
        when(medicalRecordService.deleteByName("Doe", "John"))
                .thenReturn(true);

        mockMvc.perform(delete("/medicalRecords/Doe/John"))
                .andExpect(status().isOk());
    }

    /**
     * Test that deleting a non-existent medical record returns 404.
     */
    @Test
    @DisplayName("DELETE /medicalRecords/{lastName}/{firstName} returns 404 when not found")
    void deleteMedicalRecord_whenNotFound_returns404() throws Exception {
        when(medicalRecordService.deleteByName("Doe", "John"))
                .thenReturn(false);

        mockMvc.perform(delete("/medicalRecords/Doe/John"))
                .andExpect(status().isNotFound());
    }
}
