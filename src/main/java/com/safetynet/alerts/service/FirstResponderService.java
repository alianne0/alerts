package com.safetynet.alerts.service;

import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.domain.Person;
import com.safetynet.alerts.dto.*;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     *
     * @param data
     */
    @Autowired
    public FirstResponderService(DataParser data) {
        this.data = data;
    }

    private static String normalizeName(String firstName, String lastName) {
        return ((firstName) + "|" + (lastName)).toLowerCase(Locale.ROOT).trim();
    }


    /**
     * Helper method for computing the age
     *
     * @param mr
     * @return
     */
    private static Integer computeAge(MedicalRecord mr) {
        if (mr == null) return null;
        try {
            String birthdate = mr.getBirthdate();
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
     * First finds all the addresses and errors out if the station number does not exist
     * Then it filters the people who live at those addresses, then does a medical record lookup
     * Finally it maps the data to the DTO and counts the adults/children
     *
     * @param stationNumber
     * @return
     */
    public PeoplePerStation getPeopleByStation(String stationNumber) {
        List<String> addresses = new ArrayList<>();
        for (Firestation fs : data.getFirestations()) {
            if (fs != null && fs.getStation().equals(stationNumber)) {
                addresses.add(fs.getAddress());
            }
        }

        if (addresses.isEmpty()) {
            throw new IllegalArgumentException(
                    "Firestation not found for station number: " + stationNumber
            );
        }

        List<Person> personsCovered = new ArrayList<>();
        for (Person p : data.getPersons()) {
            if (p != null && addresses.contains(p.getAddress())) {
                personsCovered.add(p);
            }
        }

        Map<String, MedicalRecord> medicalRecordSearch = new HashMap<>();
        for (MedicalRecord mr : data.getMedicalRecords()) {
            if (mr != null && mr.getFirstName() != null && mr.getLastName() != null) {
                String key = normalizeName(mr.getFirstName(), mr.getLastName());
                medicalRecordSearch.put(key, mr);
            }
        }

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

            if (age != null) {
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
     * First find the people living at an address and errors if its unknown
     * Then builds lookup for medical records and determines which people are children by age
     * Finally it obtains other household members by searching by address
     */
    public ChildrenByAddress getChildrenByAddress(String address) {
        List<Person> personsAtAddress = new ArrayList<>();
        for (Person p : data.getPersons()) {
            if (p != null && address.equalsIgnoreCase(p.getAddress())) {
                personsAtAddress.add(p);
            }
        }

        if (personsAtAddress.isEmpty()) {
            throw new IllegalArgumentException(
                    "Address not found: " + address
            );
        }

        Map<String, MedicalRecord> recordLookup = new HashMap<>();
        for (MedicalRecord mr : data.getMedicalRecords()) {
            if (mr != null && mr.getFirstName() != null && mr.getLastName() != null) {
                String key = normalizeName(mr.getFirstName(), mr.getLastName());
                recordLookup.put(key, mr);
            }
        }

        List<ChildrenByAddressDTO> children = new ArrayList<>();

        for (Person p : personsAtAddress) {
            String key = normalizeName(p.getFirstName(), p.getLastName());
            MedicalRecord mr = recordLookup.get(key);

            Integer age = computeAge(mr);
            if (age != null && age <= 18) {

                ChildrenByAddressDTO childDto =
                        new ChildrenByAddressDTO(p.getFirstName(), p.getLastName(), age);

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

        return new ChildrenByAddress(children);
    }

    /**
     * Returns a list of unique phone numbers for residents served by the given fire station.
     * First finds all addresses for that firestation, then filters the people who live at those addresses
     * Extracts unique, non-blank numbers and then sorts them
     *
     * @param fireStation the fire station number
     * @return PhonesPerStation view containing the list of phone numbers
     */
    public PhonesPerStation getPhonesPerStation(String fireStation) {
        List<String> addresses = new ArrayList<>();
        for (Firestation fs : data.getFirestations()) {
            if (fs != null && fs.getStation() != null && fs.getStation().equals(fireStation)) {
                if (fs.getAddress() != null) {
                    addresses.add(fs.getAddress());
                }
            }
        }

        if (addresses.isEmpty()) {
            throw new IllegalArgumentException(
                    "Firestation not found for station number: " + fireStation
            );
        }

        List<Person> personsCovered = new ArrayList<>();
        for (Person p : data.getPersons()) {
            if (p != null && p.getAddress() != null && addresses.contains(p.getAddress())) {
                personsCovered.add(p);
            }
        }

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

        List<String> phones = new ArrayList<>(uniquePhones);
        Collections.sort(phones);

        return new PhonesPerStation(phones);
    }

    /**
     * Returns the list of residents living at the given address and the fire station number serving it.
     * First finds the station number associated with the address
     * Filter the people who live at that address
     * Also maps medical record information to that person
     *
     * @param address the address to search
     * @return ResidentsPerAddress view containing the residents and the station number
     */
    public ResidentsPerAddress getResidentsPerAddress(String address) {
        String stationNumber = null;
        for (Firestation fs : data.getFirestations()) {
            if (fs != null && fs.getAddress() != null && fs.getAddress().equals(address)) {
                stationNumber = fs.getStation();
                break;
            }
        }

        if (stationNumber == null) {
            throw new IllegalArgumentException(
                    "Address not found: " + address
            );
        }

        List<Person> residentsAtAddress = new ArrayList<>();
        for (Person p : data.getPersons()) {
            if (p != null && address != null && address.equals(p.getAddress())) {
                residentsAtAddress.add(p);
            }
        }

        Map<String, MedicalRecord> medicalRecordSearch = new HashMap<>();
        for (MedicalRecord mr : data.getMedicalRecords()) {
            if (mr != null && mr.getFirstName() != null && mr.getLastName() != null) {
                String key = normalizeName(mr.getFirstName(), mr.getLastName());
                medicalRecordSearch.put(key, mr);
            }
        }

        List<ResidentsPerAddressDTO> residents = new ArrayList<>();
        for (Person p : residentsAtAddress) {
            String key = normalizeName(p.getFirstName(), p.getLastName());
            MedicalRecord mr = medicalRecordSearch.get(key);

            Integer maybeAge = computeAge(mr);
            int age = (maybeAge != null) ? maybeAge : 0;

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
     * Collects addresses for the station numbers and group by those who live at that address.
     * Builds a lookup of medical records for each person
     *
     * @param stations the list of station numbers
     * @return a list of households (one per address) with residents and their medical info
     */
    public List<HouseholdsByStation> getHouseholdsByFirestation(List<String> stations) {
        if (stations == null || stations.isEmpty()) {
            return Collections.emptyList();
        }

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

        if (addresses.isEmpty()) {
            throw new IllegalArgumentException(
                    "Firestation not found for station numbers: " + stations
            );
        }

        Map<String, List<Person>> personsByAddress = new LinkedHashMap<>();
        for (Person p : data.getPersons()) {
            if (p != null && p.getAddress() != null && addresses.contains(p.getAddress())) {
                personsByAddress
                        .computeIfAbsent(p.getAddress(), k -> new ArrayList<>())
                        .add(p);
            }
        }

        Map<String, MedicalRecord> medicalRecordSearch = new HashMap<>();
        for (MedicalRecord mr : data.getMedicalRecords()) {
            if (mr != null && mr.getFirstName() != null && mr.getLastName() != null) {
                String key = normalizeName(mr.getFirstName(), mr.getLastName());
                medicalRecordSearch.put(key, mr);
            }
        }

        List<HouseholdsByStation> households = new ArrayList<>();
        for (Map.Entry<String, List<Person>> entry : personsByAddress.entrySet()) {
            String address = entry.getKey();
            List<Person> personsAtAddress = entry.getValue();

            List<ResidentsPerAddressDTO> residents = new ArrayList<>();
            for (Person p : personsAtAddress) {
                String key = normalizeName(p.getFirstName(), p.getLastName());
                MedicalRecord mr = medicalRecordSearch.get(key);

                Integer maybeAge = computeAge(mr);
                int age = (maybeAge != null) ? maybeAge : 0;

                List<String> medications = (mr != null && mr.getMedications() != null)
                        ? mr.getMedications()
                        : Collections.emptyList();

                List<String> allergies = (mr != null && mr.getAllergies() != null)
                        ? mr.getAllergies()
                        : Collections.emptyList();

                residents.add(new ResidentsPerAddressDTO(
                        p.getFirstName(),
                        p.getLastName(),
                        p.getPhone(),
                        age,
                        medications,
                        allergies
                ));
            }
            households.add(new HouseholdsByStation(address, residents));
        }
        return households;
    }

    /**
     * Obtains the list of residents matching a given last name.
     * Returns their first/last name, address, age, email, medications (with dosages), and allergies.
     * If multiple people share the last name, they will all appear.
     *
     * @param lastName the last name to search
     * @return PersonInfo view containing a list of PersonInfoDTO
     */
    public PersonInfo getPersonInfo(String lastName) {

        if (lastName == null || lastName.isBlank()) {
            return new PersonInfo(new ArrayList<>());
        }

        Map<String, MedicalRecord> medicalRecordSearch = new HashMap<>();
        for (MedicalRecord mr : data.getMedicalRecords()) {
            if (mr != null && mr.getFirstName() != null && mr.getLastName() != null) {
                String key = normalizeName(mr.getFirstName(), mr.getLastName());
                medicalRecordSearch.put(key, mr);
            }
        }

        List<Person> personsMatched = new ArrayList<>();
        for (Person p : data.getPersons()) {
            if (p != null && p.getLastName() != null) {
                if (p.getLastName().equalsIgnoreCase(lastName)) {
                    personsMatched.add(p);
                }
            }
        }

        if (personsMatched.isEmpty()) {
            throw new IllegalArgumentException(
                    "No person found with last name: " + lastName
            );
        }

        List<PersonInfoDTO> result = new ArrayList<>();
        for (Person p : personsMatched) {
            String key = normalizeName(p.getFirstName(), p.getLastName());
            MedicalRecord mr = medicalRecordSearch.get(key);

            Integer ageObj = computeAge(mr);
            int age = (ageObj != null) ? ageObj : 0;

            List<String> medications =
                    (mr != null && mr.getMedications() != null)
                            ? mr.getMedications()
                            : Collections.emptyList();

            List<String> allergies =
                    (mr != null && mr.getAllergies() != null)
                            ? mr.getAllergies()
                            : Collections.emptyList();

            PersonInfoDTO dto = new PersonInfoDTO(
                    p.getLastName(),
                    p.getFirstName(),
                    p.getAddress(),
                    age,
                    p.getEmail(),
                    medications,
                    allergies
            );
            result.add(dto);
        }
        return new PersonInfo(result);
    }

    /**
     * Obtains unique email addresses for all residents in the given city.
     * Case-insensitive city match. Keeps stable order by first appearance.
     *
     * @param city the city to search
     * @return ResidentEmails view containing a unique list of emails
     */
    public ResidentEmails getResidentEmails(String city) {
        if (city == null || city.isBlank()) {
            return new ResidentEmails(new ArrayList<>());
        }

        Set<String> uniqueEmails = new LinkedHashSet<>();
        for (Person p : data.getPersons()) {
            if (p == null) continue;

            String personCity = p.getCity();
            if (personCity != null && personCity.equalsIgnoreCase(city)) {
                String email = p.getEmail();
                if (email != null && !email.isBlank()) {
                    uniqueEmails.add(email);
                }
            }
        }

        if (uniqueEmails.isEmpty()) {
            throw new IllegalArgumentException(
                    "City not found: " + city
            );
        }

        List<String> emails = new ArrayList<>(uniqueEmails);

        return new ResidentEmails(emails);
    }
}
