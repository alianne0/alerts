package com.safetynet.alerts.service;

import com.safetynet.alerts.domain.Firestation;
import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.domain.Person;
import com.safetynet.alerts.dto.ChildrenByAddressDTO;
import com.safetynet.alerts.dto.HouseholdMembersDTO;
import com.safetynet.alerts.dto.PersonInfoDTO;
import com.safetynet.alerts.repository.DataParser;
import com.safetynet.alerts.view.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the FirstResponderService class
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirstResponderServiceTest {

    @Mock
    private DataParser dataParser;
    private FirstResponderService service;
    private List<Person> persons;
    private List<Firestation> firestations;
    private List<MedicalRecord> medicalRecords;

    private static Firestation fs(String address, String station) {
        Firestation f = new Firestation();
        f.setAddress(address);
        f.setStation(station);
        return f;
    }

    private static Person p(String first, String last, String address, String phone) {
        Person p = new Person();
        p.setFirstName(first);
        p.setLastName(last);
        p.setAddress(address);
        p.setPhone(phone);
        return p;
    }

    private static MedicalRecord mr(String first, String last, int age) {
        MedicalRecord mr = new MedicalRecord();
        mr.setFirstName(first);
        mr.setLastName(last);
        mr.setBirthdate(
                LocalDate.now()
                        .minusYears(age)
                        .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        );
        return mr;
    }

    /**
     * Initializes the service with mocked data lists before each
     */
    @BeforeEach
    void setUp() {
        persons = new ArrayList<>();
        firestations = new ArrayList<>();
        medicalRecords = new ArrayList<>();

        when(dataParser.getPersons()).thenReturn(persons);
        when(dataParser.getFirestations()).thenReturn(firestations);
        when(dataParser.getMedicalRecords()).thenReturn(medicalRecords);

        service = new FirstResponderService(dataParser);
    }

    /**
     * Tests related to getting people covered by a station
     */
    @Nested
    class GetPeopleByStation {

        /**
         * Correctly returns the people and has the right adult and child count
         */
        @Test
        @DisplayName("getPeopleByStation returns people and correct adult/child counts")
        void returnsPeopleAndCounts() {
            firestations.add(fs("1509 Culver St", "3"));

            persons.add(p("John", "Doe", "1509 Culver St", "111"));
            persons.add(p("Jane", "Doe", "1509 Culver St", "222"));

            medicalRecords.add(mr("John", "Doe", 40));
            medicalRecords.add(mr("Jane", "Doe", 10));

            PeoplePerStation result = service.getPeopleByStation("3");

            assertThat(result.getCoveredPersons()).hasSize(2);
            assertThat(result.getAdultCount()).isEqualTo(1);
            assertThat(result.getChildCount()).isEqualTo(1);
        }

        /**
         * Proper failing and error handling when a station does not exist
         */
        @Test
        @DisplayName("getPeopleByStation throws when station does not exist")
        void stationNotFound() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.getPeopleByStation("99"))
                    .withMessageContaining("Firestation not found");
        }
    }

    /**
     * Tests for get children by address
     * Also will contain household members
     */
    @Nested
    class GetChildrenByAddress {

        /**
         * Tests that the associated child is returned and contains the household members
         */
        @Test
        @DisplayName("getChildrenByAddress returns children with household members")
        void returnsChildrenWithHousehold() {
            persons.add(p("John", "Doe", "1509 Culver St", "111"));
            persons.add(p("Jane", "Doe", "1509 Culver St", "222"));

            medicalRecords.add(mr("John", "Doe", 45));
            medicalRecords.add(mr("Jane", "Doe", 12));

            ChildrenByAddress result =
                    service.getChildrenByAddress("1509 Culver St");

            assertThat(result.getChildren()).hasSize(1);

            ChildrenByAddressDTO child = result.getChildren().get(0);
            assertThat(child.getFirstName()).isEqualTo("Jane");
            assertThat(child.getHouseholdMembersList())
                    .extracting(HouseholdMembersDTO::getFirstName)
                    .containsExactly("John");
        }

        /**
         * Errors out when there is an unknown address input
         */
        @Test
        @DisplayName("getChildrenByAddress throws when address is unknown")
        void addressNotFound() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.getChildrenByAddress("Unknown"))
                    .withMessageContaining("Address not found");
        }
    }

    /**
     * Tests related to getting phone numbers by station
     */
    @Nested
    class GetPhonesPerStation {

        /**
         * Successfully return the phones per station
         */
        @Test
        @DisplayName("getPhonesPerStation returns unique sorted phone numbers")
        void returnsSortedUniquePhones() {
            firestations.add(fs("A", "1"));

            persons.add(p("A", "One", "A", "999"));
            persons.add(p("B", "Two", "A", "111"));
            persons.add(p("C", "Three", "A", "999"));

            PhonesPerStation result = service.getPhonesPerStation("1");

            assertThat(result.getPhones())
                    .containsExactly("111", "999");
        }
    }

    /**
     * Tests related to get residents and station by the provided address
     */
    @Nested
    class GetResidentsPerAddress {

        /**
         * Successfully matches the address and returns the
         * associated residents and the station number
         */
        @Test
        @DisplayName("getResidentsPerAddress returns residents and station number")
        void returnsResidentsAndStation() {
            firestations.add(fs("1509 Culver St", "3"));

            persons.add(p("John", "Doe", "1509 Culver St", "111"));
            medicalRecords.add(mr("John", "Doe", 50));

            ResidentsPerAddress result =
                    service.getResidentsPerAddress("1509 Culver St");

            assertThat(result.getStationNumber()).isEqualTo("3");
            assertThat(result.getResidents()).hasSize(1);
        }
    }

    /**
     * Tests related to grouping households by firestation
     */
    @Nested
    class GetHouseholdsByFirestation {

        /**
         * Makes sure that households are grouped by address
         * when multiple people share the same location
         */
        @Test
        @DisplayName("getHouseholdsByFirestation groups residents by address")
        void groupsByAddress() {
            firestations.add(fs("Addr1", "1"));

            persons.add(p("John", "Doe", "Addr1", "111"));
            persons.add(p("Jane", "Doe", "Addr1", "222"));

            medicalRecords.add(mr("John", "Doe", 40));
            medicalRecords.add(mr("Jane", "Doe", 10));

            List<HouseholdsByStation> result =
                    service.getHouseholdsByFirestation(List.of("1"));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getResidents()).hasSize(2);
        }
    }

    /**
     * Tests related to getting a person info by last name
     */
    @Nested
    class GetPersonInfo {

        /**
         * Tests that it returns the personInfo for the matching input
         * last name, and has medical record info too
         */
        @Test
        @DisplayName("getPersonInfo returns all matching people for last name")
        void returnsMatchingPeople() {
            persons.add(p("John", "Doe", "Addr1", "111"));
            persons.add(p("Jane", "Doe", "Addr2", "222"));

            medicalRecords.add(mr("John", "Doe", 40));
            medicalRecords.add(mr("Jane", "Doe", 12));

            PersonInfo result = service.getPersonInfo("Doe");

            assertThat(result.getPersonInfoDTOList())
                    .hasSize(2)
                    .extracting(PersonInfoDTO::getFirstName)
                    .containsExactlyInAnyOrder("John", "Jane");
        }

        /**
         * Makes sure last names matches
         */
        @Test
        @DisplayName("getPersonInfo matches last name ignoring case")
        void matchesIgnoringCase() {
            persons.add(p("John", "Doe", "Addr", "111"));
            medicalRecords.add(mr("John", "Doe", 30));

            PersonInfo result = service.getPersonInfo("doe");

            assertThat(result.getPersonInfoDTOList()).hasSize(1);
            assertThat(result.getPersonInfoDTOList().get(0).getLastName()).isEqualTo("Doe");
        }

        /**
         //         * Blank or null last names should not throw,
         //         * but simply return an empty result.
         //         */
//        @Test
//        @DisplayName("getPersonInfo returns empty list for null or blank input")
//        void nullOrBlankLastName() {
//            assertThat(service.getPersonInfo(null).getPersonInfoDTOList()).isEmpty();
//            assertThat(service.getPersonInfo("   ").getPersonInfoDTOList()).isEmpty();
//        }

        /**
         * Test will fail if there are no matching people
         */
        @Test
        @DisplayName("getPersonInfo throws when no person matches last name")
        void noMatchesFound() {
            persons.add(p("John", "Smith", "Addr", "111"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.getPersonInfo("Doe"))
                    .withMessageContaining("No person found");
        }
    }

    /**
     * Tests related to getting resident emails by city
     */
    @Nested
    class GetResidentEmails {

        /**
         * Tests proper return for the emails in a given city
         */
        @Test
        @DisplayName("getResidentEmails returns unique emails for matching city")
        void returnsUniqueEmails() {
            Person p1 = p("John", "Doe", "Addr1", "111");
            p1.setCity("Cleveland");
            p1.setEmail("a@test.com");

            Person p2 = p("Jane", "Doe", "Addr2", "222");
            p2.setCity("Cleveland");
            p2.setEmail("b@test.com");

            Person p3 = p("Jim", "Doe", "Addr3", "333");
            p3.setCity("Cleveland");
            p3.setEmail("a@test.com"); // duplicate

            persons.add(p1);
            persons.add(p2);
            persons.add(p3);

            ResidentEmails result = service.getResidentEmails("Cleveland");

            assertThat(result.getEmails())
                    .containsExactly("a@test.com", "b@test.com");
        }

        /**
         * City matching should be case-insensitive.
         */
        @Test
        @DisplayName("getResidentEmails matches city ignoring case")
        void matchesCityIgnoringCase() {
            Person p = p("John", "Doe", "Addr", "111");
            p.setCity("Cleveland");
            p.setEmail("test@test.com");

            persons.add(p);

            ResidentEmails result = service.getResidentEmails("cleveland");

            assertThat(result.getEmails()).containsExactly("test@test.com");
        }

        /**
         * Null or blank city input should return an empty list
         * instead of throwing an exception.
         */
        @Test
        @DisplayName("getResidentEmails returns empty list for null or blank city")
        void nullOrBlankCity() {
            assertThat(service.getResidentEmails(null).getEmails()).isEmpty();
            assertThat(service.getResidentEmails("   ").getEmails()).isEmpty();
        }

        /**
         * Errors out if there is no matching city
         */
        @Test
        @DisplayName("getResidentEmails throws when city is not found")
        void cityNotFound() {
            Person p = p("John", "Doe", "Addr", "111");
            p.setCity("OtherCity");
            p.setEmail("test@test.com");

            persons.add(p);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.getResidentEmails("Cleveland"))
                    .withMessageContaining("City not found");
        }
    }
}