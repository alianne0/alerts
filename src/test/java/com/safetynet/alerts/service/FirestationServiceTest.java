package com.safetynet.alerts.service;

import com.safetynet.alerts.domain.Firestation;
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
import static org.mockito.Mockito.when;

/**
 * Unit tests for the Firestation Service class
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirestationServiceTest {

    @Mock
    private DataParser dataParser;
    private FirestationService service;
    private List<Firestation> backingList;

    private static Firestation fs(String address, String station) {
        Firestation f = new Firestation();
        f.setAddress(address);
        f.setStation(station);
        return f;
    }

    /**
     * Initializes the service with a mocked DataParser and
     * a fresh backing list before each test.
     */
    @BeforeEach
    void setUp() {
        backingList = new ArrayList<>();
        when(dataParser.getFirestations()).thenReturn(backingList);
        service = new FirestationService(dataParser);
    }


    /**
     * Tests the findAll function
     */
    @Test
    @DisplayName("findAll returns an unmodifiable copy of existing firestations")
    void findAll_returnsUnmodifiableCopy() {
        backingList.add(fs("1509 Culver St", "3"));
        backingList.add(fs("29 15th St", "2"));

        var result = service.findAll();

        assertThat(result)
                .hasSize(2)
                .extracting(Firestation::getAddress)
                .containsExactly("1509 Culver St", "29 15th St");

        assertThatThrownBy(() -> result.add(fs("X", "Y")))
                .isInstanceOf(UnsupportedOperationException.class);

        backingList.add(fs("New Addr", "9"));
        assertThat(result).hasSize(2);
    }

    /**
     * Tests the findByAddress function
     * Rrturns a match in addresses
     */
    @Test
    @DisplayName("findByAddress matches with trimming and is null-safe")
    void findByAddress_trimAndNullSafe() {
        backingList.add(fs(" 1509 Culver St ", "3"));
        backingList.add(fs("Other", "1"));

        assertThat(service.findByAddress("1509 Culver St")).isPresent();
        assertThat(service.findByAddress("  1509 Culver St  ")).isPresent();
        assertThat(service.findByAddress("missing")).isNotPresent();
        assertThat(service.findByAddress(null)).isNotPresent();
    }

    /**
     * Tests the findByStation function
     * Returns a match in station numbers
     */
    @Test
    @DisplayName("findByStation matches with trimming and is null-safe")
    void findByStation_trimAndNullSafe() {
        backingList.add(fs("A", " 3 "));
        backingList.add(fs("B", "2"));

        assertThat(service.findByStation("3")).isPresent();
        assertThat(service.findByStation("  3  ")).isPresent();
        assertThat(service.findByStation("7")).isNotPresent();
        assertThat(service.findByStation(null)).isNotPresent();
    }

    /**
     * Test deleting address is successful
     */
    @Test
    @DisplayName("deleteByAddress removes matching entry using trimmed address")
    void deleteByAddress_trimMatch() {
        backingList.add(fs(" 1509 Culver St ", "3"));
        backingList.add(fs("Other", "1"));

        boolean removed = service.deleteByAddress("1509 Culver St");
        assertThat(removed).isTrue();
        assertThat(backingList)
                .extracting(Firestation::getAddress)
                .containsExactly("Other");

        boolean removedNone = service.deleteByAddress("nope");
        assertThat(removedNone).isFalse();
    }

    /**
     * Several tests that pertain to updating a firestation mapping
     */
    @Nested
    class PostFirestation {

        /**
         * Test rejecting null input
         */
        @Test
        @DisplayName("postFirestation throws NullPointerException for null input")
        void post_nullInput() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.postFirestation(null))
                    .withMessage("firestation must not be null");
        }

        /**
         * Test null/blank addresses are not accepted
         */
        @Test
        @DisplayName("postFirestation rejects null or blank address")
        void post_nullOrBlankAddress() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.postFirestation(fs(null, "1")))
                    .withMessage("address cannot be null");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.postFirestation(fs("   ", "1")))
                    .withMessage("address cannot be null");
        }

        /**
         * Test rejecting null/blank station params
         */
        @Test
        @DisplayName("postFirestation rejects null or blank station")
        void post_nullOrBlankStation() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.postFirestation(fs("Addr", null)))
                    .withMessage("station cannot be null");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.postFirestation(fs("Addr", "   ")))
                    .withMessage("station cannot be null");
        }

        /**
         * Test that updating a firestation with an existing address
         */
        @Test
        @DisplayName("postFirestation updates existing mapping by trimmed address")
        void post_updatesExisting() {
            backingList.add(fs("1509 Culver St", "3"));

            Firestation toPost = fs(" 1509 Culver St  ", "7");
            Firestation returned = service.postFirestation(toPost);

            assertThat(returned.getStation()).isEqualTo("7");
            assertThat(backingList)
                    .hasSize(1)
                    .first()
                    .extracting(Firestation::getStation)
                    .isEqualTo("7");
        }

        /**
         * Test update a firestation with new address
         */
        @Test
        @DisplayName("postFirestation adds new mapping when address does not exist")
        void post_addsNew() {
            backingList.add(fs("Existing", "1"));

            Firestation toPost = fs("New Address", "5");
            Firestation returned = service.postFirestation(toPost);

            assertThat(backingList).hasSize(2);
            assertThat(backingList)
                    .extracting(Firestation::getAddress)
                    .containsExactlyInAnyOrder("Existing", "New Address");
            assertThat(returned).isSameAs(toPost);
        }
    }

    /**
     * Includes several tests regarding updating a firestation
     */
    @Nested
    class UpdateFirestation {

        /**
         * Test rejecting null params
         */
        @Test
        @DisplayName("updateFirestation throws NullPointerException for null arguments")
        void update_nullArgs() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.updateFirestation(null, "1"))
                    .withMessage("address must not be null");

            assertThatNullPointerException()
                    .isThrownBy(() -> service.updateFirestation("Addr", null))
                    .withMessage("newStation must not be null");
        }

        /**
         * Ensures blank station values are rejected
         */
        @Test
        @DisplayName("updateFirestation throws IllegalArgumentException for blank station")
        void update_blankNewStation() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.updateFirestation("Addr", "   "))
                    .withMessage("newStation must not be blank");
        }

        /**
         * Test that an existing mapping is correctly updated
         * Also checks that an unknown address results in an Optional that is empty
         */
        @Test
        @DisplayName("updateFirestation updates matching address and returns Optional.of")
        void update_success() {
            backingList.add(fs(" 1509 Culver St ", "3"));
            backingList.add(fs("Other", "1"));

            Optional<Firestation> updated =
                    service.updateFirestation("1509 Culver St", " 7 ");

            assertThat(updated).isPresent();
            assertThat(updated.get().getStation()).isEqualTo("7");
            assertThat(backingList.get(0).getStation()).isEqualTo("7");

            Optional<Firestation> notFound =
                    service.updateFirestation("Unknown", "9");

            assertThat(notFound).isEmpty();
        }
    }
}
