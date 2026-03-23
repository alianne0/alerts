package com.safetynet.alerts.controller;

import com.safetynet.alerts.service.FirstResponderService;
import com.safetynet.alerts.view.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the FirstResponderController class
 */
@ExtendWith(MockitoExtension.class)
class FirstResponderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FirstResponderService firstResponderService;

    @InjectMocks
    private FirstResponderController firstResponderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(firstResponderController)
                .build();
    }

    /**
     * Test successflly getting people by station
     *
     * @throws Exception
     */
    @Test
    void getPeopleByStation_shouldReturnOk() throws Exception {
        when(firstResponderService.getPeopleByStation("1"))
                .thenReturn(Mockito.mock(PeoplePerStation.class));

        mockMvc.perform(get("/firestation")
                        .param("stationNumber", "1"))
                .andExpect(status().isOk());
    }

    /**
     * Test successfully getting children by address
     *
     * @throws Exception
     */
    @Test
    void getChildrenByAddress_shouldReturnOk() throws Exception {
        when(firstResponderService.getChildrenByAddress("1509 Culver St"))
                .thenReturn(Mockito.mock(ChildrenByAddress.class));

        mockMvc.perform(get("/childAlert")
                        .param("address", "1509 Culver St"))
                .andExpect(status().isOk());
    }

    /**
     * Test getting the phones per station
     *
     * @throws Exception
     */
    @Test
    void getPhonesPerStation_shouldReturnOk() throws Exception {
        when(firstResponderService.getPhonesPerStation("2"))
                .thenReturn(Mockito.mock(PhonesPerStation.class));

        mockMvc.perform(get("/phoneAlert")
                        .param("firestation", "2"))
                .andExpect(status().isOk());
    }

    /**
     * Tests getting the residents per address
     *
     * @throws Exception
     */
    @Test
    void getResidentsPerAddress_shouldReturnOk() throws Exception {
        when(firstResponderService.getResidentsPerAddress("29 15th St"))
                .thenReturn(Mockito.mock(ResidentsPerAddress.class));

        mockMvc.perform(get("/fire")
                        .param("address", "29 15th St"))
                .andExpect(status().isOk());
    }

    /**
     * Test getting the households by firestation
     *
     * @throws Exception
     */
    @Test
    void getHouseholdsByFirestation_shouldReturnOk() throws Exception {
        when(firstResponderService.getHouseholdsByFirestation(List.of("1", "2")))
                .thenReturn(List.of(Mockito.mock(HouseholdsByStation.class)));

        mockMvc.perform(get("/flood")
                        .param("stations", "1", "2"))
                .andExpect(status().isOk());
    }

    /**
     * Tests getting the person info
     *
     * @throws Exception
     */
    @Test
    void getPersonInfo_shouldReturnOk() throws Exception {
        when(firstResponderService.getPersonInfo("Doe"))
                .thenReturn(Mockito.mock(PersonInfo.class));

        mockMvc.perform(get("/personInfo")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk());
    }

    /**
     * Tests getting the resident emails
     *
     * @throws Exception
     */
    @Test
    void getResidentEmails_shouldReturnOk() throws Exception {
        when(firstResponderService.getResidentEmails("Culver"))
                .thenReturn(Mockito.mock(ResidentEmails.class));

        mockMvc.perform(get("/communityEmail")
                        .param("city", "Culver"))
                .andExpect(status().isOk());
    }
}
