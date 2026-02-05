package com.safetynet.alerts.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.domain.Person;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DataParser {

    // Path to the external JSON file (configured in application.properties)
    private final Path filePath;
    private final ObjectMapper mapper;

    // Keep these MUTABLE so services can modify them in memory
    private List<Person> persons = new ArrayList<>();
    private List<Firestation> firestations = new ArrayList<>();
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    public DataParser(@Value("${data.file:./data/data.json}") String filePath) {
        this.filePath = Paths.get(filePath).toAbsolutePath().normalize();
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void load() {
        try {
            // Create file & parent directories on first run if missing
            if (Files.notExists(filePath)) {
                Files.createDirectories(filePath.getParent());
                // initialize an empty wrapper and write it down
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

    // --- Accessors return the LIVE lists (services can mutate them) ---
    public List<Person> getPersons() { return persons; }
    public List<Firestation> getFirestations() { return firestations; }
    public List<MedicalRecord> getMedicalRecords() { return medicalRecords; }

    /**
     * Persist current in-memory state to disk safely.
     * Synchronized to avoid concurrent writes from multiple requests/threads.
     */
    public synchronized void saveToFile() {
        try {
            DataWrapper wrapper = new DataWrapper();
            wrapper.setPersons(this.persons);
            wrapper.setFirestations(this.firestations);
            wrapper.setMedicalrecords(this.medicalRecords);
            writeWrapperAtomically(wrapper);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save to " + filePath, e);
        }
    }

    /**
     * Write to a temporary file first, then atomically move into place.
     * Prevents corrupted JSON if the app crashes during write.
     */
    private void writeWrapperAtomically(DataWrapper wrapper) throws IOException {
        Path tmp = Files.createTempFile(filePath.getParent(), "data-", ".json.tmp");
        try {
            mapper.writeValue(tmp.toFile(), wrapper);
            Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            // In case move fails, ensure temp is cleaned up
            try { Files.deleteIfExists(tmp); } catch (Exception ignore) {}
        }
    }

    // Wrapper matches the structure of data.json
    public static class DataWrapper {
        private List<Person> persons;
        private List<Firestation> firestations;
        private List<MedicalRecord> medicalrecords;

        public List<Person> getPersons() { return persons; }
        public void setPersons(List<Person> persons) { this.persons = persons; }

        public List<Firestation> getFirestations() { return firestations; }
        public void setFirestations(List<Firestation> firestations) { this.firestations = firestations; }

        public List<MedicalRecord> getMedicalrecords() { return medicalrecords; }
        public void setMedicalrecords(List<MedicalRecord> medicalrecords) { this.medicalrecords = medicalrecords; }
    }
}