package com.safetynet.alerts.service;

import com.safetynet.alerts.domain.*;
import com.safetynet.alerts.dto.CoveredPersonsDTO;
import com.safetynet.alerts.view.PeoplePerStation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.safetynet.alerts.repository.DataParser;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service class for our first responder endpoints
 */
@Service
public class FirstResponderService {
    private final DataParser data;

    /**
     * Constructor for the first responder service and instantiates our data parser
     * @param data
     */
    @Autowired
    public FirstResponderService(DataParser data) { this.data = data; }

    private static String normalizeName(String firstName, String lastName) {
        return ((firstName) + "|" + (lastName)).toLowerCase(Locale.ROOT).trim();
    }

    private static Integer computeAge(MedicalRecord mr) {
        if (mr == null) return null;
        try {
            String birthdate = mr.getBirthdate(); // e.g., "03/06/1984"
            if (birthdate != null && !birthdate.isBlank()) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                LocalDate dob = LocalDate.parse(birthdate, fmt);
                return Period.between(dob, LocalDate.now()).getYears();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Obtains the list of people serviced by a particular fire station number
     * Finds the addresses for a fire station, then finds the people with that address
     * Counts the adults and children by lookup of birthdate by name
     * @param stationNumber
     * @return
     */
    public PeoplePerStation getPeopleByStation(String stationNumber) {
        // find all addresses for this firestation
        List<String> addresses = new ArrayList<>();
        for(Firestation fs : data.getFirestations()){
            if(fs != null && fs.getStation().equals(stationNumber)) {
                addresses.add(fs.getAddress());
            }
        }
        //filter persons who live at those addresses
        List<Person> personsCovered = new ArrayList<>();
        for(Person p : data.getPersons()){
            if(p != null &&  addresses.contains(p.getAddress())) {
                personsCovered.add(p);
            }
        }

        //count adults and children (requires medical records)
        //linear lookup of birthday by name
        Map<String, MedicalRecord> medicalRecordSearch = new HashMap<>();
        for (MedicalRecord mr : data.getMedicalRecords()){
            if (mr != null && mr.getFirstName() != null && mr.getLastName() != null) {

                String key = normalizeName(mr.getFirstName(), mr.getLastName());
                medicalRecordSearch.put(key, mr);
            }
        }

        //map them to DTO
        List<CoveredPersonsDTO> personToDto = new ArrayList<>();
        int adultCount = 0;
        int childCount = 0;
        for (Person p : personsCovered) {
            CoveredPersonsDTO dto = new CoveredPersonsDTO(
                    p.getFirstName(),
                    p.getLastName(),
                    p.getAddress(),
                    p.getPhone()
            );
            personToDto.add(dto);
            String key = normalizeName(p.getFirstName(), p.getLastName());
            MedicalRecord mr = medicalRecordSearch.get(key);
            Integer age = computeAge(mr);
            if(age != null) {
                if (age < 18) {
                    childCount++;
                } else {
                    adultCount++;
                }
            }
        }
        return new PeoplePerStation(personToDto, adultCount, childCount);
    }
}
