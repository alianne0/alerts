package com.safetynet.alerts;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class PersonControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void testShowPerson() throws Exception {
        mockMvc.perform(get("/person")).andExpect(status().is2xxSuccessful());
//                .andExpect(view().name("People"))
//                .andExpect(model().size(23))
//                .andExpect(model().attributeExists("firstName"));
//
    }

}
