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

/**
 * Unit tests for the MedicalRecordService class
 */
@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private DataParser dataParser;
    private MedicalRecordService service;
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

    /**
     * Initializes the service with a mocked DataParser.
     * No stubbing is to avoid unnecessary stubbing errors.
     */
    @BeforeEach
    void setUp() {
        backingList = new ArrayList<>();
        service = new MedicalRecordService(dataParser);
    }

    private void enableBackingList() {
        lenient().when(dataParser.getMedicalRecords())
                .thenAnswer(invocation -> backingList);
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    /**
     * Test that the function findAll returns the list of medical records
     */
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
        assertThat(result)
                .extracting(MedicalRecord::getLastName)
                .containsExactly("Doe", "Smith");

        assertThatThrownBy(() -> result.add(new MedicalRecord()))
                .isInstanceOf(UnsupportedOperationException.class);

        backingList.add(mr("Brown", "Bob", "03/03/1993", List.of(), List.of()));
        assertThat(result).hasSize(2);
    }

    /**
     * Test creating a new medical record
     */
    @Nested
    class PostMedicalRecord {

        /**
         * Ensures null inputs are not accepted
         */
        @Test
        @DisplayName("postMedicalRecord throws on null input")
        void post_null() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.postMedicalRecord(null))
                    .withMessage("medical record cannot be null");
        }

        /**
         * Test that you can add a new medical record when one does not already exist
         */
        @Test
        @DisplayName("postMedicalRecord adds when no matching full name exists")
        void post_addsNew() {
            enableBackingList();
            backingList.add(mr("Doe", "John", "01/01/1990",
                    List.of("a:10mg"), List.of("pollen")));

            MedicalRecord toPost =
                    mr("Roe", "Jane", "02/02/1992",
                            List.of("b:5mg"), List.of("peanut"));

            MedicalRecord returned = service.postMedicalRecord(toPost);

            assertThat(backingList).hasSize(2);
            assertThat(returned).isSameAs(toPost);
            assertThat(backingList)
                    .extracting(MedicalRecord::getLastName)
                    .containsExactlyInAnyOrder("Doe", "Roe");
        }

        /**
         * Test that is you post a medical record with a matching name it replaces it
         */
        @Test
        @DisplayName("postMedicalRecord updates existing record when names match")
        void post_updatesExistingOnNameMatch() {
            enableBackingList();
            MedicalRecord existing = mr("Doe", "John", "01/01/1990",
                    new ArrayList<>(List.of("a:10mg")),
                    new ArrayList<>(List.of("pollen")));
            backingList.add(existing);

            MedicalRecord toPost =
                    mr("  Doe ", " John  ", "02/02/1992",
                            List.of("b:5mg"), List.of("peanut"));

            MedicalRecord returned = service.postMedicalRecord(toPost);

            assertThat(returned).isSameAs(existing);
            assertThat(existing.getBirthdate()).isEqualTo("02/02/1992");
            assertThat(existing.getMedications()).containsExactly("b:5mg");
            assertThat(existing.getAllergies()).containsExactly("peanut");
            assertThat(backingList).hasSize(1);
        }
    }

    /**
     * Tests related to updating existing medical records.
     */
    @Nested
    class UpdateMedicalRecordTests {

        /**
         * Rejecting null inputs
         */
        @Test
        @DisplayName("updateMedicalRecord throws when updates is null")
        void update_nullUpdates() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.updateMedicalRecord("Doe", "John", null))
                    .withMessage("updates cant be null");
        }

        /**
         * Test that you cant change the last name
         */
        @Test
        @DisplayName("updateMedicalRecord forbids last name change")
        void update_forbidLastNameChange() {
            enableBackingList();
            backingList.add(mr("Doe", "John", "01/01/1990",
                    List.of("a"), List.of("x")));

            MedicalRecord updates =
                    mr("Different", null, null, List.of("b"), List.of("y"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.updateMedicalRecord("Doe", "John", updates))
                    .withMessage("last name cannot be changed");
        }

        /**
         * Test that you cannot change the first name
         */
        @Test
        @DisplayName("updateMedicalRecord forbids first name change")
        void update_forbidFirstNameChange() {
            enableBackingList();
            backingList.add(mr("Doe", "John", "01/01/1990",
                    List.of("a"), List.of("x")));

            MedicalRecord updates =
                    mr(null, "Johnny", null, List.of("b"), List.of("y"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.updateMedicalRecord("Doe", "John", updates))
                    .withMessage("first name cannot be changed");
        }

        /**
         * Test that you can update all fields
         */
        @Test
        @DisplayName("updateMedicalRecord updates all fields and returns Optional.of")
        void update_successWithBirthdate() {
            enableBackingList();
            MedicalRecord existing = mr("Doe", "John", "01/01/1990",
                    new ArrayList<>(List.of("a")),
                    new ArrayList<>(List.of("x")));
            backingList.add(existing);

            MedicalRecord updates =
                    mr("  Doe ", " John ", "02/02/1992",
                            List.of("b", "c"), List.of("y"));

            Optional<MedicalRecord> result =
                    service.updateMedicalRecord("Doe", "John", updates);

            assertThat(result).isPresent();
            assertThat(existing.getBirthdate()).isEqualTo("02/02/1992");
            assertThat(existing.getMedications()).containsExactly("b", "c");
            assertThat(existing.getAllergies()).containsExactly("y");
        }

        /**
         * Can update medications and allergies even if birthday is missing
         */
        @Test
        @DisplayName("updateMedicalRecord mutates meds/allergies but returns empty when birthdate is null")
        void update_partialWithoutBirthdate_returnsEmptyButMutates() {
            enableBackingList();
            MedicalRecord existing = mr("Doe", "John", "01/01/1990",
                    new ArrayList<>(List.of("a")),
                    new ArrayList<>(List.of("x")));
            backingList.add(existing);

            MedicalRecord updates =
                    mr("Doe", "John", null,
                            List.of("b", "c"), List.of("y"));

            Optional<MedicalRecord> result =
                    service.updateMedicalRecord("Doe", "John", updates);

            assertThat(result).isEmpty();
            assertThat(existing.getBirthdate()).isEqualTo("01/01/1990");
            assertThat(existing.getMedications()).containsExactly("b", "c");
            assertThat(existing.getAllergies()).containsExactly("y");
        }

        /**
         * Test no matching record returning empty
         */
        @Test
        @DisplayName("updateMedicalRecord returns empty when no match exists")
        void update_noMatch() {
            enableBackingList();
            backingList.add(mr("Roe", "Jane", "02/02/1992", List.of(), List.of()));

            MedicalRecord updates =
                    mr(null, null, "03/03/1993", List.of("z"), List.of("n"));

            Optional<MedicalRecord> result =
                    service.updateMedicalRecord("Doe", "John", updates);

            assertThat(result).isEmpty();
        }

        /**
         * Test the trim method
         */
        @Test
        @DisplayName("updateMedicalRecord matches names using trimming")
        void update_trimMatching() {
            enableBackingList();
            MedicalRecord existing = mr("  Doe ", " John ", "01/01/1990",
                    new ArrayList<>(List.of("a")),
                    new ArrayList<>(List.of("x")));
            backingList.add(existing);

            MedicalRecord updates =
                    mr("Doe", "John", "02/02/1992",
                            List.of("b"), List.of("y"));

            Optional<MedicalRecord> result =
                    service.updateMedicalRecord(" Doe", "John ", updates);

            assertThat(result).isPresent();
            assertThat(existing.getBirthdate()).isEqualTo("02/02/1992");
            assertThat(existing.getMedications()).containsExactly("b");
            assertThat(existing.getAllergies()).containsExactly("y");
        }
    }

    /**
     * Tests related to deleting medical records by name
     */
    @Nested
    class DeleteTests {

        /**
         * Test deletion is trim-sensitive in matching records
         */
        @Test
        @DisplayName("deleteByName removes matching record using trim-aware comparison")
        void delete_trimAware() {
            enableBackingList();
            backingList.add(mr("  Doe ", " John ", "01/01/1990",
                    List.of("a"), List.of("x")));
            backingList.add(mr("Smith", "Jane", "02/02/1992",
                    List.of(), List.of()));

            boolean removed = service.deleteByName("Doe", "John");
            assertThat(removed).isTrue();
            assertThat(backingList).hasSize(1);
            assertThat(backingList.get(0).getLastName()).isEqualTo("Smith");

            boolean removedNone = service.deleteByName("Unknown", "Person");
            assertThat(removedNone).isFalse();
        }
    }
}