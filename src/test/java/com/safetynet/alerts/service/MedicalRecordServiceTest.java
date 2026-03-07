package com.safetynet.alerts.service;
import com.safetynet.alerts.domain.MedicalRecord;
import com.safetynet.alerts.repository.DataParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private DataParser dataParser;

    private MedicalRecordService service;

    // Will act as the in-memory store returned by the mocked DataParser
    private List<MedicalRecord> backingList;

    private static MedicalRecord mr(
            String lastName,
            String firstName,
            String birthdate,
            List<String> medications,
            List<String> allergies
    ) {
        MedicalRecord m = new MedicalRecord();
        m.setLastName(lastName);
        m.setFirstName(firstName);
        m.setBirthdate(birthdate);
        m.setMedications(medications);
        m.setAllergies(allergies);
        return m;
    }

    @BeforeEach
    void setUp() {
        backingList = new ArrayList<>();
        service = new MedicalRecordService(dataParser); // no stubbing yet
    }

    /** Only stub DataParser in tests that actually call it, to avoid UnnecessaryStubbingException */
    private void enableBackingList() {
        lenient().when(dataParser.getMedicalRecords()).thenAnswer(invocation -> backingList);
    }

    @Test
    @DisplayName("findAll returns an unmodifiable copy and is not affected by later mutations")
    void findAll_unmodifiableCopy() {
        enableBackingList();
        backingList.add(mr("Doe", "John", "01/01/1990",
                Arrays.asList("med1", "med2"), Arrays.asList("pollen")));
        backingList.add(mr("Smith", "Jane", "02/02/1992",
                List.of(), Arrays.asList("peanut")));

        var result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MedicalRecord::getLastName).containsExactly("Doe", "Smith");

        assertThatThrownBy(() -> result.add(new MedicalRecord()))
                .isInstanceOf(UnsupportedOperationException.class);

        // mutate backing list and ensure previous result is unaffected
        backingList.add(mr("Brown", "Bob", "03/03/1993", List.of(), List.of()));
        assertThat(result).hasSize(2);
    }

    @Nested
    class PostMedicalRecord {

        @Test
        @DisplayName("postMedicalRecord throws on null input")
        void post_null() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.postMedicalRecord(null))
                    .withMessage("medical record cannot be null");
        }

        @Test
        @DisplayName("postMedicalRecord adds when no matching full name exists")
        void post_addsNew() {
            enableBackingList();
            backingList.add(mr("Doe", "John", "01/01/1990", List.of("a:10mg"), List.of("pollen")));

            MedicalRecord toPost = mr("Roe", "Jane", "02/02/1992", List.of("b:5mg"), List.of("peanut"));
            MedicalRecord returned = service.postMedicalRecord(toPost);

            assertThat(backingList).hasSize(2);
            assertThat(returned).isSameAs(toPost);
            assertThat(backingList).extracting(MedicalRecord::getLastName)
                    .containsExactlyInAnyOrder("Doe", "Roe");
        }

        @Test
        @DisplayName("postMedicalRecord updates existing record when first+last names match (trim-safe)")
        void post_updatesExistingOnNameMatch() {
            enableBackingList();
            MedicalRecord existing = mr("Doe", "John", "01/01/1990",
                    new ArrayList<>(List.of("a:10mg")), new ArrayList<>(List.of("pollen")));
            backingList.add(existing);

            MedicalRecord toPost = mr("  Doe ", " John  ", "02/02/1992",
                    List.of("b:5mg"), List.of("peanut"));

            MedicalRecord returned = service.postMedicalRecord(toPost);

            // Returned is the same object that now reflects new values
            assertThat(returned).isSameAs(existing);
            assertThat(existing.getBirthdate()).isEqualTo("02/02/1992");
            assertThat(existing.getMedications()).containsExactly("b:5mg");
            assertThat(existing.getAllergies()).containsExactly("peanut");
            assertThat(existing.getFirstName()).isEqualTo(" John  ");
            assertThat(existing.getLastName()).isEqualTo("  Doe ");
            assertThat(backingList).hasSize(1);
        }
    }

    @Nested
    class UpdateMedicalRecordTests {

        @Test
        @DisplayName("updateMedicalRecord throws when updates is null")
        void update_nullUpdates() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.updateMedicalRecord("Doe", "John", null))
                    .withMessage("updates cant be null");
        }

        @Test
        @DisplayName("updateMedicalRecord forbids last name change")
        void update_forbidLastNameChange() {
            enableBackingList();
            backingList.add(mr("Doe", "John", "01/01/1990",
                    List.of("a"), List.of("x")));

            MedicalRecord updates = mr("Different", null, null,
                    List.of("b"), List.of("y"));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.updateMedicalRecord("Doe", "John", updates))
                    .withMessage("last name cannot be changed");
        }

        @Test
        @DisplayName("updateMedicalRecord forbids first name change")
        void update_forbidFirstNameChange() {
            enableBackingList();
            backingList.add(mr("Doe", "John", "01/01/1990",
                    List.of("a"), List.of("x")));

            MedicalRecord updates = mr(null, "Johnny", null,
                    List.of("b"), List.of("y"));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.updateMedicalRecord("Doe", "John", updates))
                    .withMessage("first name cannot be changed");
        }

        @Test
        @DisplayName("updateMedicalRecord updates meds/allergies and birthdate when name matches (trim-safe) and returns Optional.of(updated)")
        void update_successWithBirthdate() {
            enableBackingList();
            MedicalRecord existing = mr("Doe", "John", "01/01/1990",
                    new ArrayList<>(List.of("a")), new ArrayList<>(List.of("x")));
            backingList.add(existing);

            MedicalRecord updates = mr("  Doe ", " John ", "02/02/1992",
                    List.of("b", "c"), List.of("y"));

            Optional<MedicalRecord> result = service.updateMedicalRecord("Doe", "John", updates);

            assertThat(result).isPresent();
            MedicalRecord updated = result.get();
            assertThat(updated).isSameAs(existing);
            assertThat(updated.getBirthdate()).isEqualTo("02/02/1992");
            assertThat(updated.getMedications()).containsExactly("b", "c");
            assertThat(updated.getAllergies()).containsExactly("y");
        }

        @Test
        @DisplayName("updateMedicalRecord mutates meds/allergies when birthdate is null, but returns Optional.empty() (captures current behavior)")
        void update_partialWithoutBirthdate_returnsEmptyButMutates() {
            enableBackingList();
            MedicalRecord existing = mr("Doe", "John", "01/01/1990",
                    new ArrayList<>(List.of("a")), new ArrayList<>(List.of("x")));
            backingList.add(existing);

            // Only medications and allergies updated; birthdate left null
            MedicalRecord updates = mr("Doe", "John", null,
                    List.of("b", "c"), List.of("y"));

            Optional<MedicalRecord> result = service.updateMedicalRecord("Doe", "John", updates);

            // Current implementation returns Optional.empty() if birthdate is null,
            // even though meds/allergies mutations occur.
            assertThat(result).isEmpty();

            // Verify side effects occurred
            assertThat(existing.getBirthdate()).isEqualTo("01/01/1990");
            assertThat(existing.getMedications()).containsExactly("b", "c");
            assertThat(existing.getAllergies()).containsExactly("y");
        }

        @Test
        @DisplayName("updateMedicalRecord returns empty when no match")
        void update_noMatch() {
            enableBackingList(); // This test will hit the repository after passing validation.
            // Repo contains some other record that doesn't match
            backingList.add(mr("Roe", "Jane", "02/02/1992", List.of(), List.of()));

            // Do NOT attempt to change names. Leave them null or set them to the same values as params.
            MedicalRecord updates = mr(null, null, "03/03/1993", List.of("z"), List.of("n"));

            Optional<MedicalRecord> result = service.updateMedicalRecord("Doe", "John", updates);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("updateMedicalRecord matches names with trimming")
        void update_trimMatching() {
            enableBackingList();
            MedicalRecord existing = mr("  Doe ", " John ", "01/01/1990",
                    new ArrayList<>(List.of("a")), new ArrayList<>(List.of("x")));
            backingList.add(existing);

            MedicalRecord updates = mr("Doe", "John", "02/02/1992",
                    List.of("b"), List.of("y"));

            Optional<MedicalRecord> result = service.updateMedicalRecord(" Doe", "John ", updates);

            assertThat(result).isPresent();
            assertThat(existing.getBirthdate()).isEqualTo("02/02/1992");
            assertThat(existing.getMedications()).containsExactly("b");
            assertThat(existing.getAllergies()).containsExactly("y");
        }
    }

    @Nested
    class DeleteTests {

        @Test
        @DisplayName("deleteByName removes matching record using trim-aware comparison")
        void delete_trimAware() {
            enableBackingList();
            backingList.add(mr("  Doe ", " John ", "01/01/1990", List.of("a"), List.of("x")));
            backingList.add(mr("Smith", "Jane", "02/02/1992", List.of(), List.of()));

            boolean removed = service.deleteByName("Doe", "John");
            assertThat(removed).isTrue();
            assertThat(backingList).hasSize(1);
            assertThat(backingList.get(0).getLastName()).isEqualTo("Smith");

            boolean removedNone = service.deleteByName("Unknown", "Person");
            assertThat(removedNone).isFalse();
        }
    }
}
