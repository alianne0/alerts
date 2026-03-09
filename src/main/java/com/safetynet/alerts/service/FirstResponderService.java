package com.safetynet.alerts.service;

import com.safetynet.alerts.domain.*;
import com.safetynet.alerts.dto.CoveredPersonsDTO;
import com.safetynet.alerts.dto.ChildrenByAddressDTO;
import com.safetynet.alerts.dto.HouseholdMembersDTO;
import com.safetynet.alerts.dto.ResidentsPerAddressDTO;
import com.safetynet.alerts.view.*;
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

    /**
     * Returns children (age 18 or younger) living at the given address,
     * including their names, ages, and a list of other household members.
     * Returns an empty result object if no children are found.
     */
    public ChildrenByAddress getChildrenByAddress(String address) {

        // 1. Find persons living at the given address
        List<Person> personsAtAddress = new ArrayList<>();
        for (Person p : data.getPersons()) {
            if (p != null && address.equalsIgnoreCase(p.getAddress())) {
                personsAtAddress.add(p);
            }
        }

        // 2. Build lookup map for medical records
        Map<String, MedicalRecord> recordLookup = new HashMap<>();
        for (MedicalRecord mr : data.getMedicalRecords()) {
            if (mr != null && mr.getFirstName() != null && mr.getLastName() != null) {
                String key = normalizeName(mr.getFirstName(), mr.getLastName());
                recordLookup.put(key, mr);
            }
        }

        // 3. Determine which persons are children
        List<ChildrenByAddressDTO> children = new ArrayList<>();

        for (Person p : personsAtAddress) {
            String key = normalizeName(p.getFirstName(), p.getLastName());
            MedicalRecord mr = recordLookup.get(key);

            Integer age = computeAge(mr);
            if (age != null && age <= 18) {

                // Child DTO
                ChildrenByAddressDTO childDto =
                        new ChildrenByAddressDTO(p.getFirstName(), p.getLastName(), age);

                // Build household member list (everyone except child)
                List<HouseholdMembersDTO> householdMembers = new ArrayList<>();
                for (Person other : personsAtAddress) {
                    if (!other.getFirstName().equalsIgnoreCase(p.getFirstName()) ||
                            !other.getLastName().equalsIgnoreCase(p.getLastName())) {

                        householdMembers.add(new HouseholdMembersDTO(
                                other.getFirstName(),
                                other.getLastName()
                        ));
                    }
                }

                childDto.setHouseholdMembersList(householdMembers);
                children.add(childDto);
            }
        }

        // 4. Wrap in view object to match your other endpoint conventions
        return new ChildrenByAddress(children);
    }

    /**
     * Returns a list of unique phone numbers for residents served by the given fire station.
     * @param fireStation the fire station number
     * @return PhonesPerStation view containing the list of phone numbers
     */
    public PhonesPerStation getPhonesPerStation(String fireStation) {
        // 1) find all addresses for this firestation
        List<String> addresses = new ArrayList<>();
        for (Firestation fs : data.getFirestations()) {
            if (fs != null && fs.getStation() != null && fs.getStation().equals(fireStation)) {
                if (fs.getAddress() != null) {
                    addresses.add(fs.getAddress());
                }
            }
        }

        // 2) filter persons who live at those addresses
        List<Person> personsCovered = new ArrayList<>();
        for (Person p : data.getPersons()) {
            if (p != null && p.getAddress() != null && addresses.contains(p.getAddress())) {
                personsCovered.add(p);
            }
        }

        // 3) extract unique, non-blank phone numbers
        // use LinkedHashSet to dedupe while keeping insertion order (stable for tests)
        Set<String> uniquePhones = new LinkedHashSet<>();
        for (Person p : personsCovered) {
            String phone = (p != null) ? p.getPhone() : null;
            if (phone != null) {
                phone = phone.trim();
                if (!phone.isEmpty()) {
                    uniquePhones.add(phone);
                }
            }
        }

        // 4) sort for deterministic responses
        List<String> phones = new ArrayList<>(uniquePhones);
        Collections.sort(phones);

        return new PhonesPerStation(phones);
    }

    /**
     * Returns the list of residents living at the given address and the fire station number serving it.
     *
     * @param address the address to search
     * @return ResidentsPerAddress view containing the residents and the station number
     */
    public ResidentsPerAddress getResidentsPerAddress(String address) {
        // 1) find the fire station number for this address (first match wins)
        String stationNumber = null;
        for (Firestation fs : data.getFirestations()) {
            if (fs != null && fs.getAddress() != null && fs.getAddress().equals(address)) {
                stationNumber = fs.getStation();
                break;
            }
        }

        // 2) filter persons who live at this address
        List<Person> residentsAtAddress = new ArrayList<>();
        for (Person p : data.getPersons()) {
            if (p != null && address != null && address.equals(p.getAddress())) {
                residentsAtAddress.add(p);
            }
        }

        // 3) build a lookup of medical records by normalized full name
        Map<String, MedicalRecord> medicalRecordSearch = new HashMap<>();
        for (MedicalRecord mr : data.getMedicalRecords()) {
            if (mr != null && mr.getFirstName() != null && mr.getLastName() != null) {
                String key = normalizeName(mr.getFirstName(), mr.getLastName());
                medicalRecordSearch.put(key, mr);
            }
        }

        // 4) map residents to DTOs: name, phone, age, medications, allergies
        List<ResidentsPerAddressDTO> residents = new ArrayList<>();
        for (Person p : residentsAtAddress) {
            String key = normalizeName(p.getFirstName(), p.getLastName());
            MedicalRecord mr = medicalRecordSearch.get(key);

            Integer maybeAge = computeAge(mr);
            int age = (maybeAge != null) ? maybeAge : 0; // DTO uses primitive int

            List<String> medications = (mr != null && mr.getMedications() != null)
                    ? mr.getMedications()
                    : Collections.emptyList();

            List<String> allergies = (mr != null && mr.getAllergies() != null)
                    ? mr.getAllergies()
                    : Collections.emptyList();

            ResidentsPerAddressDTO dto = new ResidentsPerAddressDTO(
                    p.getFirstName(),
                    p.getLastName(),
                    p.getPhone(),
                    age,
                    medications,
                    allergies
            );
            residents.add(dto);
        }

        return new ResidentsPerAddress(residents, stationNumber);
    }
    /**
     * Returns all households served by the given fire station numbers.
     *
     * @param stations the list of station numbers
     * @return a list of households (one per address) with residents and their medical info
     */
    public List<HouseholdsByStation> getHouseholdsByFirestation(List<String> stations) {
        // Guard clauses (optional, adjust to your project standards)
        if (stations == null || stations.isEmpty()) {
            return Collections.emptyList();
        }

        // 1) Collect all addresses for the provided station numbers
        Set<String> stationSet = new HashSet<>(stations);
        Set<String> addresses = new LinkedHashSet<>();
        for (Firestation fs : data.getFirestations()) {
            if (fs != null
                    && fs.getStation() != null
                    && stationSet.contains(fs.getStation())
                    && fs.getAddress() != null) {
                addresses.add(fs.getAddress());
            }
        }

        // 2) Group persons who live at those addresses by address
        Map<String, List<Person>> personsByAddress = new LinkedHashMap<>();
        for (Person p : data.getPersons()) {
            if (p != null && p.getAddress() != null && addresses.contains(p.getAddress())) {
                personsByAddress.computeIfAbsent(p.getAddress(), k -> new ArrayList<>()).add(p);
            }
        }

        // 3) Build a lookup of medical records by normalized full name
        Map<String, MedicalRecord> medicalRecordSearch = new HashMap<>();
        for (MedicalRecord mr : data.getMedicalRecords()) {
            if (mr != null && mr.getFirstName() != null && mr.getLastName() != null) {
                String key = normalizeName(mr.getFirstName(), mr.getLastName());
                medicalRecordSearch.put(key, mr);
            }
        }

        // 4) For each address, map residents to DTOs: name, phone, age, medications, allergies
        List<HouseholdsByStation> households = new ArrayList<>();
        for (Map.Entry<String, List<Person>> entry : personsByAddress.entrySet()) {
            String address = entry.getKey();
            List<Person> personsAtAddress = entry.getValue();

            List<ResidentsPerAddressDTO> residents = new ArrayList<>();
            for (Person p : personsAtAddress) {
                String key = normalizeName(p.getFirstName(), p.getLastName());
                MedicalRecord mr = medicalRecordSearch.get(key);

                Integer maybeAge = computeAge(mr);
                int age = (maybeAge != null) ? maybeAge : 0; // DTO uses primitive int

                List<String> medications = (mr != null && mr.getMedications() != null)
                        ? mr.getMedications()
                        : Collections.emptyList();

                List<String> allergies = (mr != null && mr.getAllergies() != null)
                        ? mr.getAllergies()
                        : Collections.emptyList();

                ResidentsPerAddressDTO dto = new ResidentsPerAddressDTO(
                        p.getFirstName(),
                        p.getLastName(),
                        p.getPhone(),
                        age,
                        medications,
                        allergies
                );
                residents.add(dto);
            }
            households.add(new HouseholdsByStation(address, residents));
        }
        return households;
    }
}
