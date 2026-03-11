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
        log.debug("DataParser instantiated with filePath={}", this.filePath);
    }

    /**
     * Loads data from the JSON file at startup.
     * Creates an empty file if none exists.
     */
    @PostConstruct
    public void load() {
        log.info("Initializing data load from {}", filePath);
        try {
            if (Files.notExists(filePath)) {
                log.warn("Data file does not exist at {}. Creating a new empty data file.", filePath);
                Files.createDirectories(filePath.getParent());
                DataWrapper empty = new DataWrapper();
                empty.setPersons(new ArrayList<>());
                empty.setFirestations(new ArrayList<>());
                empty.setMedicalrecords(new ArrayList<>());
                writeWrapperAtomically(empty);
                log.info("Created new empty data file at {}", filePath);
            }

            DataWrapper data = mapper.readValue(Files.newInputStream(filePath), DataWrapper.class);

            this.persons = new ArrayList<>(Optional.ofNullable(data.getPersons()).orElseGet(ArrayList::new));
            this.firestations = new ArrayList<>(Optional.ofNullable(data.getFirestations()).orElseGet(ArrayList::new));
            this.medicalRecords = new ArrayList<>(Optional.ofNullable(data.getMedicalrecords()).orElseGet(ArrayList::new));

            log.info("Data load complete from {}", filePath);
            log.debug("Loaded counts: persons={}, firestations={}, medicalRecords={}",
                    persons.size(), firestations.size(), medicalRecords.size());
        } catch (Exception e) {
            log.error("Failed to load data from {}", filePath, e);
            throw new RuntimeException("Failed to load " + filePath, e);
        }
    }

    /**
     * Saves all in‑memory data to the JSON file.
     * Synchronized to avoid concurrent writes.
     */
    public void saveToFile() {
        log.info("Persisting data to {}", filePath);
        try {
            DataWrapper wrapper = new DataWrapper();
            wrapper.setPersons(this.persons);
            wrapper.setFirestations(this.firestations);
            wrapper.setMedicalrecords(this.medicalRecords);

            log.debug("Persisting counts: persons={}, firestations={}, medicalRecords={}",
                    (persons != null ? persons.size() : 0),
                    (firestations != null ? firestations.size() : 0),
                    (medicalRecords != null ? medicalRecords.size() : 0));

            writeWrapperAtomically(wrapper);
            log.info("Persisted data successfully to {}", filePath);
        } catch (IOException e) {
            log.error("Failed to save to file {}", filePath, e);
            throw new RuntimeException("Failed to save to " + filePath, e);
        }
    }

    /**
     * Writes data to a temporary file and replaces the original atomically.
     * Prevents partial or corrupted JSON writes.
     */
    private void writeWrapperAtomically(DataWrapper wrapper) throws IOException {
        Path tmp = Files.createTempFile(filePath.getParent(), "data-", ".json.tmp");
        log.debug("Writing to temporary file {} before atomic move to {}", tmp, filePath);
        try {
            mapper.writeValue(tmp.toFile(), wrapper);
            Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            log.debug("Atomic move to {} completed", filePath);
        } finally {
            try {
                if (Files.deleteIfExists(tmp)) {
                    log.trace("Temporary file {} deleted", tmp);
                }
            } catch (Exception ignore) {
                log.warn("Could not delete temporary file {}", tmp, ignore);
            }
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
