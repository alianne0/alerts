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

@Data
@Slf4j
@Repository
public class DataParser {

    private final Path filePath;
    private final ObjectMapper mapper;

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
            if (Files.notExists(filePath)) {
                createEmptyFile();
            }

            DataWrapper data =
                    mapper.readValue(Files.newInputStream(filePath), DataWrapper.class);

            if (data.getPersons() != null) {
                persons = new ArrayList<>(data.getPersons());
            }
            if (data.getFirestations() != null) {
                firestations = new ArrayList<>(data.getFirestations());
            }
            if (data.getMedicalrecords() != null) {
                medicalRecords = new ArrayList<>(data.getMedicalrecords());
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to load data file: " + filePath, e);
        }
    }

    public synchronized void saveToFile() {
        try {
            DataWrapper wrapper = new DataWrapper();
            wrapper.setPersons(persons);
            wrapper.setFirestations(firestations);
            wrapper.setMedicalrecords(medicalRecords);

            writeAtomically(wrapper);

        } catch (IOException e) {
            throw new RuntimeException("Unable to save data file: " + filePath, e);
        }
    }

    private void createEmptyFile() throws IOException {
        Files.createDirectories(filePath.getParent());

        DataWrapper empty = new DataWrapper();
        empty.setPersons(new ArrayList<>());
        empty.setFirestations(new ArrayList<>());
        empty.setMedicalrecords(new ArrayList<>());

        writeAtomically(empty);
    }

    private void writeAtomically(DataWrapper wrapper) throws IOException {
        Path tempFile =
                Files.createTempFile(filePath.getParent(), "data-", ".json");

        try {
            mapper.writeValue(tempFile.toFile(), wrapper);
            Files.move(
                    tempFile,
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Data
    public static class DataWrapper {
        private List<Person> persons;
        private List<Firestation> firestations;
        private List<MedicalRecord> medicalrecords;
    }
}