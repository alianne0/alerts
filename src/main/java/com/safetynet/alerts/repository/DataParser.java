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

/**
 * Handles loading and saving of application data
 * Only the working file is modified. The original file is not modified
 */
@Data
@Slf4j
@Repository
public class DataParser {

    private final Path originalPath;
    private final Path workingPath;
    private final ObjectMapper mapper;

    private List<Person> persons = new ArrayList<>();
    private List<Firestation> firestations = new ArrayList<>();
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    public DataParser(
            @Value("${data.file:~/data.json}") String original,
            @Value("${data.working-file:~/data-working.json}") String working
    ) {
        this.originalPath = Paths.get(original).toAbsolutePath().normalize();
        this.workingPath = Paths.get(working).toAbsolutePath().normalize();
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Loads data when the application starts
     * If a working file already exists, then load from that
     * Otherwise  initialize the working file from the original seed file
     */
    @PostConstruct
    public void load() {
        try {
            Files.createDirectories(workingPath.getParent());

            if (Files.exists(workingPath)) {
                log.info("Loading existing working data file: {}", workingPath);
                loadFromPath(workingPath);
            } else {
                log.info("Working file not found. Creating it from original: {}", originalPath);

                if (!Files.exists(originalPath)) {
                    throw new RuntimeException("Original data.json is missing: " + originalPath);
                }

                Files.copy(originalPath, workingPath, StandardCopyOption.REPLACE_EXISTING);
                loadFromPath(workingPath);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load data.", e);
        }
    }

    /**
     * Loads data from the given JSON file path
     */
    private void loadFromPath(Path path) throws IOException {
        DataWrapper wrapper = mapper.readValue(Files.newInputStream(path), DataWrapper.class);

        persons = wrapper.getPersons() != null ? new ArrayList<>(wrapper.getPersons()) : new ArrayList<>();
        firestations = wrapper.getFirestations() != null ? new ArrayList<>(wrapper.getFirestations()) : new ArrayList<>();
        medicalRecords = wrapper.getMedicalrecords() != null ? new ArrayList<>(wrapper.getMedicalrecords()) : new ArrayList<>();

        log.info("Loaded {} persons, {} firestations, {} medical records.",
                persons.size(), firestations.size(), medicalRecords.size());
    }

    /**
     * Saves the current in-memory data to the working JSON file
     */
    public synchronized void saveToFile() {
        try {
            DataWrapper wrapper = new DataWrapper();
            wrapper.setPersons(persons);
            wrapper.setFirestations(firestations);
            wrapper.setMedicalrecords(medicalRecords);

            writeAtomically(workingPath, wrapper);
            log.info("Data successfully written to: {}", workingPath);

        } catch (IOException e) {
            throw new RuntimeException("Failed to write working data file.", e);
        }
    }

    /**
     * Writes JSON to disk using a temp file
     */
    private void writeAtomically(Path target, DataWrapper wrapper) throws IOException {
        Path temp = Files.createTempFile(target.getParent(), "data-", ".json");

        try {
            mapper.writeValue(temp.toFile(), wrapper);

            Files.move(
                    temp,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /**
     * JSON structure
     */
    @Data
    public static class DataWrapper {
        private List<Person> persons;
        private List<Firestation> firestations;
        private List<MedicalRecord> medicalrecords;
    }
}