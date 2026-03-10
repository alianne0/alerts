package com.safetynet.alerts.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.domain.Person;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles loading and saving SafetyNet data from a JSON file.
 * Stores the data in memory so services can modify it.
 */
@Data
@Slf4j
@Repository
public class DataParser {

    private final Path filePath;
    private final ObjectMapper mapper;

    // Mutable in‑memory data lists
    private List<Person> persons = new ArrayList<>();
    private List<Firestation> firestations = new ArrayList<>();
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    /**
     * Creates a DataParser using the configured JSON file path.
     *
     * @param filePath path to the JSON data file
     */
    public DataParser(@Value("${data.file:./data/data.json}") String filePath) {
        this.filePath = Paths.get(filePath).toAbsolutePath().normalize();
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Loads data from the JSON file at startup.
     * Creates an empty file if none exists.
     */
    @PostConstruct
    public void load() {
        try {
            if (Files.notExists(filePath)) {
                Files.createDirectories(filePath.getParent());
                DataWrapper empty = new DataWrapper();
                empty.setPersons(new ArrayList<>());
                empty.setFirestations(new ArrayList<>());
                empty.setMedicalrecords(new ArrayList<>());
                writeWrapperAtomically(empty);
            }

            DataWrapper data = mapper.readValue(Files.newInputStream(filePath), DataWrapper.class);

            this.persons = new ArrayList<>(Optional.ofNullable(data.getPersons()).orElseGet(ArrayList::new));
            this.firestations = new ArrayList<>(Optional.ofNullable(data.getFirestations()).orElseGet(ArrayList::new));
            this.medicalRecords = new ArrayList<>(Optional.ofNullable(data.getMedicalrecords()).orElseGet(ArrayList::new));

            System.out.printf(
                    "[DataParser] Loaded: persons=%d, firestations=%d, medicalrecords=%d from %s%n",
                    persons.size(), firestations.size(), medicalRecords.size(), filePath
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + filePath, e);
        }
    }


    /**
     * Saves all in‑memory data to the JSON file.
     * Synchronized to avoid concurrent writes.
     */
    public void saveToFile() {
        try {
            DataWrapper wrapper = new DataWrapper();
            wrapper.setPersons(this.persons);
            wrapper.setFirestations(this.firestations);
            wrapper.setMedicalrecords(this.medicalRecords);
            writeWrapperAtomically(wrapper);
        } catch (IOException e) {
            //TODO: log at error level
            log.error("Failed to save to file ",  e);
            throw new RuntimeException("Failed to save to " + filePath, e);
        }
    }

    /**
     * Writes data to a temporary file and replaces the original atomically.
     * Prevents partial or corrupted JSON writes.
     */
    private void writeWrapperAtomically(DataWrapper wrapper) throws IOException {
        Path tmp = Files.createTempFile(filePath.getParent(), "data-", ".json.tmp");
        try {
            mapper.writeValue(tmp.toFile(), wrapper);
            Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            try { Files.deleteIfExists(tmp); } catch (Exception ignore) {}
        }
    }

    /**
     * Simple wrapper matching the structure of the JSON file.
     */
    @Data
    public static class DataWrapper {
        private List<Person> persons;
        private List<Firestation> firestations;
        private List<MedicalRecord> medicalrecords;
    }
}