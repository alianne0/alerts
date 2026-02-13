package com.safetynet.alerts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.service.FirestationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.safetynet.alerts.controller.FirestationController;
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
 * Unit tests for FirestationController using standalone MockMvc.
 */
class FirestationControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private FirestationService firestationService;
    private DataParser dataParser;

    @BeforeEach
    void setUp() {
        firestationService = mock(FirestationService.class);
        dataParser = mock(DataParser.class);
        FirestationController controller = new FirestationController(dataParser, firestationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Firestation fs(String address, String station) {
        Firestation f = new Firestation();
        f.setAddress(address);
        f.setStation(station);
        return f;
    }

    /**
     * Test get all firestations
     *
     * @throws Exception
     */
    @Test
    @DisplayName("GET /firestations returns list with 200 OK")
    void getFirestations_returnsList() throws Exception {
        List<Firestation> list = Arrays.asList(
                fs("1509 Culver St", "3"),
                fs("29 15th St", "2")
        );
        when(firestationService.findAll()).thenReturn(list);

        mockMvc.perform(get("/firestations"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].address", is("1509 Culver St")))
                .andExpect(jsonPath("$[0].station", is("3")));
        verify(firestationService).findAll();
        verifyNoMoreInteractions(firestationService);
    }

    /**
     * Test adding a new firestation via POST
     *
     * @throws Exception
     */
    @Test
    @DisplayName("POST /firestations returns 201 Created with saved entity")
    void postFirestation_created() throws Exception {
        Firestation body = fs("1509 Culver St", "3");
        when(firestationService.postFirestation(any(Firestation.class)))
                .thenReturn(fs("1509 Culver St", "3"));

        mockMvc.perform(post("/firestations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.address", is("1509 Culver St")))
                .andExpect(jsonPath("$.station", is("3")));

        verify(firestationService).postFirestation(any(Firestation.class));
    }

    /**
     * Test deleting a firestation
     */
    @Nested
    @DisplayName("DELETE /firestations/{address}")
    class DeleteTests {

        @Test
        @DisplayName("returns 200 OK when deletion succeeds")
        void delete_ok() throws Exception {
            when(firestationService.deleteByAddress("1509 Culver St")).thenReturn(true);

            mockMvc.perform(delete("/firestations/1509 Culver St"))
                    .andExpect(status().isOk());

            verify(firestationService).deleteByAddress("1509 Culver St");
        }

        @Test
        @DisplayName("returns 404 Not Found when mapping does not exist")
        void delete_notFound() throws Exception {
            when(firestationService.deleteByAddress("Unknown")).thenReturn(false);

            mockMvc.perform(delete("/firestations/Unknown"))
                    .andExpect(status().isNotFound());

            verify(firestationService).deleteByAddress("Unknown");
        }
    }

    /**
     * Test updating a firestation via PUT
     */
    @Nested
    @DisplayName("PUT /firestations/address/{address}")
    class PutStationForAddressTests {

        @Test
        @DisplayName("returns 200 OK and updated mapping when found")
        void put_ok() throws Exception {
            String address = "1509 Culver St";
            Firestation body = fs(null, "5");
            Firestation updated = fs(address, "5");

            when(firestationService.updateFirestation(eq(address), eq("5")))
                    .thenReturn(Optional.of(updated));

            mockMvc.perform(put("/firestations/address/{address}", address)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.address", is(address)))
                    .andExpect(jsonPath("$.station", is("5")));

            verify(firestationService).updateFirestation(address, "5");
        }

        @Test
        @DisplayName("returns 404 Not Found when address mapping missing")
        void put_notFound() throws Exception {
            String address = "Unknown";
            Firestation body = fs(null, "7");

            when(firestationService.updateFirestation(eq(address), eq("7")))
                    .thenReturn(Optional.empty());

            mockMvc.perform(put("/firestations/address/{address}", address)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("No firestation mapping found for address: " + address)));

            verify(firestationService).updateFirestation(address, "7");
        }

        @Test
        @DisplayName("returns 400 Bad Request when station is null/blank")
        void put_badRequest_blankStation() throws Exception {
            String address = "1509 Culver St";

            Firestation nullStation = fs(null, null);
            Firestation blankStation = fs(null, "  ");

            mockMvc.perform(put("/firestations/address/{address}", address)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nullStation)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("station must not be null or blank")));

            mockMvc.perform(put("/firestations/address/{address}", address)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(blankStation)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("station must not be null or blank")));

            verifyNoInteractions(firestationService);
        }

        @Test
        @DisplayName("returns 400 Bad Request when service throws IllegalArgumentException")
        void put_badRequest_serviceThrows() throws Exception {
            String address = "1509 Culver St";
            Firestation body = fs(null, "X");

            when(firestationService.updateFirestation(eq(address), eq("X")))
                    .thenThrow(new IllegalArgumentException("Invalid station number"));

            mockMvc.perform(put("/firestations/address/{address}", address)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Invalid station number")));

            verify(firestationService).updateFirestation(address, "X");
        }
    }
}
