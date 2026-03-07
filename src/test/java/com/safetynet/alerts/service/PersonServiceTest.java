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

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private DataParser dataParser;

    private PersonService service;

    // Mutable list simulating the in-memory persistence returned by DataParser
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

    @BeforeEach
    void setUp() {
        backingList = new ArrayList<>();
        service = new PersonService(dataParser); // no default stubbing here
    }

    /** Only stub getPersons() in tests that actually call it. */
    private void enableBackingList() {
        lenient().when(dataParser.getPersons()).thenAnswer(invocation -> backingList);
    }

    @Test
    @DisplayName("findAll returns an unmodifiable defensive copy")
    void findAll_unmodifiableCopy() {
        enableBackingList();
        backingList.add(p("Doe", "John", "1509 Culver St", "Culver", "97451", "841-874-6512", "john@doe.com"));
        backingList.add(p("Smith", "Jane", "29 15th St", "Culver", "97451", "841-874-6513", "jane@smith.com"));

        var result = service.findAll();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Person::getLastName).containsExactly("Doe", "Smith");

        assertThatThrownBy(() -> result.add(new Person()))
                .isInstanceOf(UnsupportedOperationException.class);

        // Later mutations to the backing list shouldn't affect already returned copies
        backingList.add(p("Roe", "Richard", "New", "Culver", "97451", "111-222", "r@roe.com"));
        assertThat(result).hasSize(2);
    }

    @Nested
    class FindByNameTests {

        @Test
        @DisplayName("findByName(lastName, firstName) finds a match (correct order)")
        void findByName_correctOrder() {
            enableBackingList();
            backingList.add(p("Doe", "John", "Addr", "City", "Zip", "Phone", "Email"));

            assertThat(service.findByName("Doe", "John")).isPresent();    // ✅ correct now
            assertThat(service.findByName("John", "Doe")).isNotPresent(); // ❌ incorrect order
        }

        @Test
        @DisplayName("findByName trims both stored and input names (correct order)")
        void findByName_trimmed_correctOrder() {
            enableBackingList();
            backingList.add(p("  Doe ", " John ", "Addr", "City", "Zip", "Phone", "Email"));

            assertThat(service.findByName(" Doe", "John ")).isPresent();  // trim-aware
            assertThat(service.findByName("John", "Doe")).isNotPresent(); // wrong order
        }
    }


    @Nested
    class PostPersonTests {
        @Test
        @DisplayName("postPerson throws on null input")
        void post_nullPerson() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.postPerson(null))
                    .withMessage("person must not be null");
        }

        @Test
        @DisplayName("postPerson adds a new person when no name match")
        void post_addsNew() {
            enableBackingList();
            backingList.add(p("Doe", "John", "Addr1", "City", "Zip", "Phone", "Email"));

            Person toPost = p("Roe", "Jane", "Addr2", "City2", "Zip2", "Phone2", "Email2");
            Person returned = service.postPerson(toPost);

            assertThat(backingList).hasSize(2);
            assertThat(returned).isSameAs(toPost);
            assertThat(backingList).extracting(Person::getLastName)
                    .containsExactlyInAnyOrder("Doe", "Roe");
        }

        @Test
        @DisplayName("postPerson updates existing when first+last match (trim-safe)")
        void post_updatesExisting() {
            enableBackingList();
            Person existing = p("Doe", "John", "OldAddr", "OldCity", "OldZip", "OldPhone", "old@email");
            backingList.add(existing);

            Person updates = p("  Doe ", " John ", "NewAddr", "NewCity", "NewZip", "NewPhone", "new@email");

            Person returned = service.postPerson(updates);

            assertThat(returned).isSameAs(existing);
            assertThat(existing.getAddress()).isEqualTo("NewAddr");
            assertThat(existing.getCity()).isEqualTo("NewCity");
            assertThat(existing.getZip()).isEqualTo("NewZip");
            assertThat(existing.getPhone()).isEqualTo("NewPhone");
            assertThat(existing.getEmail()).isEqualTo("new@email");

            // Names are set directly from input as well
            assertThat(existing.getFirstName()).isEqualTo(" John ");
            assertThat(existing.getLastName()).isEqualTo("  Doe ");
            assertThat(backingList).hasSize(1);
        }
    }

    @Nested
    class DeleteByNameTests {
        @Test
        @DisplayName("deleteByName removes matching record using trim-aware name comparison")
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

    @Nested
    class UpdatePersonTests {
        @Test
        @DisplayName("updatePerson throws when updates is null")
        void update_nullUpdates() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.updatePerson("Doe", "John", null))
                    .withMessage("updates cannot be null");
        }

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

        @Test
        @DisplayName("updatePerson updates fields when matching by trim-aware name and returns Optional.of(updated)")
        void update_success() {
            enableBackingList();
            Person existing = p("  Doe ", " John ", "OldAddr", "OldCity", "OldZip", "OldPhone", "old@email");
            backingList.add(existing);

            // Keep name fields null (allowed), update others
            Person updates = p(null, null, "NewAddr", "NewCity", "NewZip", "NewPhone", "new@email");

            Optional<Person> result = service.updatePerson("Doe", "John", updates);

            assertThat(result).isPresent();
            Person updated = result.get();
            assertThat(updated).isSameAs(existing);
            assertThat(updated.getAddress()).isEqualTo("NewAddr");
            assertThat(updated.getCity()).isEqualTo("NewCity");
            assertThat(updated.getZip()).isEqualTo("NewZip");
            assertThat(updated.getPhone()).isEqualTo("NewPhone");
            assertThat(updated.getEmail()).isEqualTo("new@email");
        }

        @Test
        @DisplayName("updatePerson returns empty when no match")
        void update_noMatch() {
            enableBackingList();
            backingList.add(p("Roe", "Jane", "Addr", "City", "Zip", "Phone", "Email"));

            Person updates = p(null, null, "NewAddr", null, null, null, null);

            Optional<Person> result = service.updatePerson("Doe", "John", updates);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("updatePerson accepts trimmed names and updates correctly")
        void update_trimMatching() {
            enableBackingList();
            Person existing = p("Doe", "John", "Old", "OldCity", "OldZip", "OldPhone", "old@email");
            backingList.add(existing);

            Person updates = p(null, null, "New", null, null, null, null);

            Optional<Person> result = service.updatePerson("  Doe ", " John ", updates);
            assertThat(result).isPresent();
            assertThat(existing.getAddress()).isEqualTo("New");
        }
    }
}