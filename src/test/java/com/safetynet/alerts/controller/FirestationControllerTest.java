package com.safetynet.alerts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.service.FirestationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the Firestation controller
 */

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirestationControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private FirestationService firestationService;
    private DataParser dataParser;

    @BeforeEach
    void setUp() {
        firestationService = mock(FirestationService.class);
        dataParser = mock(DataParser.class);

        FirestationController controller =
                new FirestationController(dataParser, firestationService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Small helper to keep test setup readable.
     */
    private Firestation firestation(String address, String station) {
        Firestation fs = new Firestation();
        fs.setAddress(address);
        fs.setStation(station);
        return fs;
    }

    /**
     * Test GET all firestations
     *
     * @throws Exception
     */
    @Test
    @DisplayName("GET /firestations returns all firestation mappings")
    void getFirestations_returnsAllMappings() throws Exception {
        when(firestationService.findAll()).thenReturn(List.of(
                firestation("1509 Culver St", "3"),
                firestation("29 15th St", "2")
        ));

        mockMvc.perform(get("/firestations"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].address", is("1509 Culver St")))
                .andExpect(jsonPath("$[0].station", is("3")));

        verify(firestationService).findAll();
    }

    /**
     * Test POST a new firestation mapping
     *
     * @throws Exception
     */
    @Test
    @DisplayName("POST /firestations creates a new firestation mapping")
    void postFirestation_createsMapping() throws Exception {
        Firestation request = firestation("1509 Culver St", "3");

        when(firestationService.postFirestation(any(Firestation.class)))
                .thenReturn(request);

        mockMvc.perform(post("/firestations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address").value("1509 Culver St"))
                .andExpect(jsonPath("$.station").value("3"));

        verify(firestationService).postFirestation(any(Firestation.class));
    }

    /**
     * Test DELETE a firestation mapping
     */
    @Nested
    @DisplayName("DELETE /firestations/{address}")
    class DeleteFirestationTests {

        @Test
        @DisplayName("returns 200 OK when mapping exists")
        void delete_existingMapping_returnsOk() throws Exception {
            when(firestationService.deleteByAddress("1509 Culver St"))
                    .thenReturn(true);

            mockMvc.perform(delete("/firestations/1509 Culver St"))
                    .andExpect(status().isOk());

            verify(firestationService).deleteByAddress("1509 Culver St");
        }

        @Test
        @DisplayName("returns 404 Not Found when mapping does not exist")
        void delete_missingMapping_returnsNotFound() throws Exception {
            when(firestationService.deleteByAddress("Unknown"))
                    .thenReturn(false);

            mockMvc.perform(delete("/firestations/Unknown"))
                    .andExpect(status().isNotFound());

            verify(firestationService).deleteByAddress("Unknown");
        }
    }

    /**
     * Test updating a firestation mapping via PUT
     */
    @Nested
    @DisplayName("PUT /firestations/address/{address}")
    class UpdateStationTests {

        /**
         * Tests an expsting mapping can be updated successfully
         *
         * @throws Exception
         */
        @Test
        @DisplayName("updates station number for an existing address")
        void put_updatesStation() throws Exception {
            String address = "1509 Culver St";

            Firestation request = firestation(null, "5");
            Firestation updated = firestation(address, "5");

            when(firestationService.updateFirestation(address, "5"))
                    .thenReturn(Optional.of(updated));

            mockMvc.perform(put("/firestations/address/{address}", address)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.address").value(address))
                    .andExpect(jsonPath("$.station").value("5"));

            verify(firestationService).updateFirestation(address, "5");
        }

        /**
         * Test updating an unknown address returns 404
         *
         * @throws Exception
         */
        @Test
        @DisplayName("returns 404 when address mapping does not exist")
        void put_missingAddress_returnsNotFound() throws Exception {
            String address = "Unknown";

            when(firestationService.updateFirestation(eq(address), eq("7")))
                    .thenReturn(Optional.empty());

            mockMvc.perform(put("/firestations/address/{address}", address)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firestation(null, "7"))))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(
                            containsString("No firestation mapping found for address: " + address)));

            verify(firestationService).updateFirestation(address, "7");
        }

        /**
         * Tests that invalid request bodies are rejected
         *
         * @throws Exception
         */
        @Test
        @DisplayName("returns 400 when station is null or blank")
        void put_invalidStation_returnsBadRequest() throws Exception {
            String address = "1509 Culver St";

            mockMvc.perform(put("/firestations/address/{address}", address)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firestation(null, null))))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(put("/firestations/address/{address}", address)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firestation(null, "  "))))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(firestationService);
        }

        /**
         * Test IllegalArgmentException is converted to a 400 bad request
         *
         * @throws Exception
         */
        @Test
        @DisplayName("returns 400 when service rejects the update")
        void put_serviceThrowsIllegalArgument_returnsBadRequest() throws Exception {
            String address = "1509 Culver St";

            when(firestationService.updateFirestation(address, "X"))
                    .thenThrow(new IllegalArgumentException("Invalid station number"));

            mockMvc.perform(put("/firestations/address/{address}", address)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firestation(null, "X"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Invalid station number")));

            verify(firestationService).updateFirestation(address, "X");
        }
    }
}
