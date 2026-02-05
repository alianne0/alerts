package com.safetynet.alerts.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public class JsonParser {
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream is = new ClassPathResource("data/data.json").getInputStream()) {
            DataWrapper wrapper = objectMapper.readValue(is, DataWrapper.class);

            System.out.println("Count: " + wrapper.getPersons().size());
            wrapper.getPersons().forEach(p ->
                    System.out.println(p.getFirstName() + " " + p.getLastName() + " - " + p.getEmail())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}