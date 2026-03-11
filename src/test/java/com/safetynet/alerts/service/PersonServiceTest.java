package com.safetynet.alerts.service;

import com.safetynet.alerts.domain.Person;
import com.safetynet.alerts.repository.DataParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for the PersonService
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private DataParser dataParser;
    private PersonService service;
    private List<Person> backingList;

    private static Person p(
            String lastName,
            String firstName,
            String address,
            String city,
            String zip,
            String phone,
            String email
    ) {
        Person person = new Person();
        person.setLastName(lastName);
        person.setFirstName(firstName);
        person.setAddress(address);
        person.setCity(city);
        person.setZip(zip);
        person.setPhone(phone);
        person.setEmail(email);
        return person;
    }

    /**
     * Initializes the service with a mocked DataParser.
     * No default stubbing is performed to avoid unnecessary stubbing errors.
     */
    @BeforeEach
    void setUp() {
        backingList = new ArrayList<>();
        service = new PersonService(dataParser);
    }

    private void enableBackingList() {
        lenient().when(dataParser.getPersons())
                .thenAnswer(invocation -> backingList);
    }

    /**
     * Tests the findAll function returns all of the list of people
     */
    @Test
    @DisplayName("findAll returns an unmodifiable defensive copy")
    void findAll_unmodifiableCopy() {
        enableBackingList();
        backingList.add(p("Doe", "John", "1509 Culver St", "Culver", "97451",
                "841-874-6512", "john@doe.com"));
        backingList.add(p("Smith", "Jane", "29 15th St", "Culver", "97451",
                "841-874-6513", "jane@smith.com"));

        var result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Person::getLastName)
                .containsExactly("Doe", "Smith");

        assertThatThrownBy(() -> result.add(new Person()))
                .isInstanceOf(UnsupportedOperationException.class);

        // Mutating the backing list later should not affect the returned snapshot
        backingList.add(p("Roe", "Richard", "New", "Culver", "97451",
                "111-222", "r@roe.com"));
        assertThat(result).hasSize(2);
    }

    /**
     * Test for returning persons by last name and first name
     */
    @Nested
    class FindByNameTests {

        /**
         * Test that the name matches the desired order
         */
        @Test
        @DisplayName("findByName finds a match when lastName and firstName are in correct order")
        void findByName_correctOrder() {
            enableBackingList();
            backingList.add(p("Doe", "John", "Addr", "City", "Zip", "Phone", "Email"));

            assertThat(service.findByName("Doe", "John")).isPresent();
            assertThat(service.findByName("John", "Doe")).isNotPresent();
        }

        /**
         * Test trimmming name correctly
         */
        @Test
        @DisplayName("findByName trims names and matches correctly")
        void findByName_trimmed_correctOrder() {
            enableBackingList();
            backingList.add(p("  Doe ", " John ", "Addr", "City", "Zip", "Phone", "Email"));

            assertThat(service.findByName(" Doe", "John ")).isPresent();
            assertThat(service.findByName("John", "Doe")).isNotPresent();
        }
    }

    /**
     * Tests for posting a person
     */
    @Nested
    class PostPersonTests {

        /**
         * Test null input is rejected
         */
        @Test
        @DisplayName("postPerson throws NullPointerException for null input")
        void post_nullPerson() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.postPerson(null))
                    .withMessage("person must not be null");
        }

        /**
         * Test that a new person can be added when there is not an existing person already
         */
        @Test
        @DisplayName("postPerson adds a new person when no matching name exists")
        void post_addsNew() {
            enableBackingList();
            backingList.add(p("Doe", "John", "Addr1", "City", "Zip", "Phone", "Email"));

            Person toPost =
                    p("Roe", "Jane", "Addr2", "City2", "Zip2", "Phone2", "Email2");

            Person returned = service.postPerson(toPost);

            assertThat(backingList).hasSize(2);
            assertThat(returned).isSameAs(toPost);
            assertThat(backingList)
                    .extracting(Person::getLastName)
                    .containsExactlyInAnyOrder("Doe", "Roe");
        }

        /**
         * Test that you can post a person when names match
         */
        @Test
        @DisplayName("postPerson updates existing person when names match")
        void post_updatesExisting() {
            enableBackingList();
            Person existing =
                    p("Doe", "John", "OldAddr", "OldCity", "OldZip", "OldPhone", "old@email");
            backingList.add(existing);

            Person updates =
                    p("  Doe ", " John ", "NewAddr", "NewCity", "NewZip",
                            "NewPhone", "new@email");

            Person returned = service.postPerson(updates);

            assertThat(returned).isSameAs(existing);
            assertThat(existing.getAddress()).isEqualTo("NewAddr");
            assertThat(existing.getCity()).isEqualTo("NewCity");
            assertThat(existing.getZip()).isEqualTo("NewZip");
            assertThat(existing.getPhone()).isEqualTo("NewPhone");
            assertThat(existing.getEmail()).isEqualTo("new@email");
            assertThat(backingList).hasSize(1);
        }
    }

    /**
     * Tests for deleting a person
     */
    @Nested
    class DeleteByNameTests {

        /**
         * Tests that only matching records can be deleted
         */
        @Test
        @DisplayName("deleteByName removes matching record using trim-aware comparison")
        void delete_trimAware() {
            enableBackingList();
            backingList.add(p("  Doe ", " John ", "Addr", "City", "Zip", "Phone", "Email"));
            backingList.add(p("Smith", "Jane", "Addr2", "City2", "Zip2", "Phone2", "Email2"));

            boolean removed = service.deleteByName("Doe", "John");
            assertThat(removed).isTrue();
            assertThat(backingList).hasSize(1);
            assertThat(backingList.get(0).getLastName()).isEqualTo("Smith");

            boolean removedNone = service.deleteByName("Unknown", "Person");
            assertThat(removedNone).isFalse();
        }
    }

    /**
     * Tests for updating a person
     */
    @Nested
    class UpdatePersonTests {

        /**
         * Test rejecting null values
         */
        @Test
        @DisplayName("updatePerson throws when updates is null")
        void update_nullUpdates() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.updatePerson("Doe", "John", null))
                    .withMessage("updates cannot be null");
        }

        /**
         * Test to make sure you cannot change the last name
         */
        @Test
        @DisplayName("updatePerson forbids changing last name")
        void update_forbidLastNameChange() {
            enableBackingList();
            backingList.add(p("Doe", "John", "Addr", "City", "Zip", "Phone", "Email"));

            Person updates = p("Different", null, "NewAddr", null, null, null, null);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.updatePerson("Doe", "John", updates))
                    .withMessage("last name cannot be changed");
        }

        /**
         * Test making sure you cannot change the first name
         */
        @Test
        @DisplayName("updatePerson forbids changing first name")
        void update_forbidFirstNameChange() {
            enableBackingList();
            backingList.add(p("Doe", "John", "Addr", "City", "Zip", "Phone", "Email"));

            Person updates = p(null, "Johnny", "NewAddr", null, null, null, null);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.updatePerson("Doe", "John", updates))
                    .withMessage("first name cannot be changed");
        }

        /**
         * Tests that the updated person returns when successful
         */
        @Test
        @DisplayName("updatePerson updates fields when names match and returns Optional.of")
        void update_success() {
            enableBackingList();
            Person existing =
                    p("  Doe ", " John ", "OldAddr", "OldCity", "OldZip",
                            "OldPhone", "old@email");
            backingList.add(existing);

            Person updates =
                    p(null, null, "NewAddr", "NewCity", "NewZip",
                            "NewPhone", "new@email");

            Optional<Person> result = service.updatePerson("Doe", "John", updates);

            assertThat(result).isPresent();
            assertThat(existing.getAddress()).isEqualTo("NewAddr");
            assertThat(existing.getCity()).isEqualTo("NewCity");
            assertThat(existing.getZip()).isEqualTo("NewZip");
            assertThat(existing.getPhone()).isEqualTo("NewPhone");
            assertThat(existing.getEmail()).isEqualTo("new@email");
        }

        /**
         * Test that updating a person that does not exist returns empty
         */
        @Test
        @DisplayName("updatePerson returns empty when no match exists")
        void update_noMatch() {
            enableBackingList();
            backingList.add(p("Roe", "Jane", "Addr", "City", "Zip", "Phone", "Email"));

            Person updates = p(null, null, "NewAddr", null, null, null, null);

            Optional<Person> result = service.updatePerson("Doe", "John", updates);
            assertThat(result).isEmpty();
        }

        /**
         * Test that updating a person matches the named that are trimmed
         */
        @Test
        @DisplayName("updatePerson matches trimmed names correctly")
        void update_trimMatching() {
            enableBackingList();
            Person existing =
                    p("Doe", "John", "Old", "OldCity", "OldZip", "OldPhone", "old@email");
            backingList.add(existing);

            Person updates = p(null, null, "New", null, null, null, null);

            Optional<Person> result = service.updatePerson("  Doe ", " John ", updates);

            assertThat(result).isPresent();
            assertThat(existing.getAddress()).isEqualTo("New");
        }
    }
}