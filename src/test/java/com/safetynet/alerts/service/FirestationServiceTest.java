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

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FirestationService}.
 */

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class FirestationServiceTest {

    @Mock
    private DataParser dataParser;

    private FirestationService service;

    // Backing list returned by DataParser mock; mutate it in tests to simulate persistence.
    private List<Firestation> backingList;

    private static Firestation fs(String address, String station) {
        Firestation f = new Firestation();
        f.setAddress(address);
        f.setStation(station);
        return f;
    }

    @BeforeEach
    void setUp() {
        backingList = new ArrayList<>();
        when(dataParser.getFirestations()).thenAnswer(invocation -> backingList);
        service = new FirestationService(dataParser);
    }

    @Test
    @DisplayName("findAll returns an unmodifiable copy of existing firestations")
    void findAll_unmodifiableCopy() {
        backingList.add(fs("1509 Culver St", "3"));
        backingList.add(fs("29 15th St", "2"));

        var result = service.findAll();

        assertThat(result)
                .hasSize(2)
                .extracting(Firestation::getAddress)
                .containsExactly("1509 Culver St", "29 15th St");

        // verify it's unmodifiable
        assertThatThrownBy(() -> result.add(fs("X", "Y")))
                .isInstanceOf(UnsupportedOperationException.class);

        // changing backing list later should NOT affect previously returned list
        backingList.add(fs("New Addr", "9"));
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByAddress matches with trimming and is null-safe")
    void findByAddress_trimAndNullSafe() {
        backingList.add(fs(" 1509 Culver St ", "3"));
        backingList.add(fs("Other", "1"));

        assertThat(service.findByAddress("1509 Culver St")).isPresent();
        assertThat(service.findByAddress("  1509 Culver St  ")).isPresent();
        assertThat(service.findByAddress("missing")).isNotPresent();

        // null input should not match anything (equalsTrimmed returns false)
        assertThat(service.findByAddress(null)).isNotPresent();
    }

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

    @Test
    @DisplayName("deleteByAddress removes matching by trimmed address")
    void deleteByAddress_trimMatch() {
        backingList.add(fs(" 1509 Culver St ", "3"));
        backingList.add(fs("Other", "1"));

        boolean removed = service.deleteByAddress("1509 Culver St");
        assertThat(removed).isTrue();
        assertThat(backingList).extracting(Firestation::getAddress).containsExactly("Other");

        boolean removedNone = service.deleteByAddress("nope");
        assertThat(removedNone).isFalse();
    }

    @Nested
    class PostFirestation {

        @Test
        @DisplayName("postFirestation throws on null input")
        void post_nullInput() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.postFirestation(null))
                    .withMessage("firestation must not be null");
        }

        @Test
        @DisplayName("postFirestation throws on null/blank address")
        void post_nullOrBlankAddress() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.postFirestation(fs(null, "1")))
                    .withMessage("address cannot be null");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.postFirestation(fs("   ", "1")))
                    .withMessage("address cannot be null");
        }

        @Test
        @DisplayName("postFirestation throws on null/blank station")
        void post_nullOrBlankStation() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.postFirestation(fs("Addr", null)))
                    .withMessage("station cannot be null");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.postFirestation(fs("Addr", "   ")))
                    .withMessage("station cannot be null");
        }

        @Test
        @DisplayName("postFirestation updates existing by matching address (trim-safe)")
        void post_updatesExisting() {
            backingList.add(fs("1509 Culver St", "3"));

            Firestation toPost = fs(" 1509 Culver St  ", "7"); // same address after trim, station changes
            Firestation returned = service.postFirestation(toPost);

            assertThat(returned.getAddress()).isEqualTo(" 1509 Culver St  "); // service sets exact value from input
            assertThat(returned.getStation()).isEqualTo("7");

            // ensure the object in the list is the updated one (service updates in place)
            assertThat(backingList)
                    .hasSize(1)
                    .first()
                    .extracting(Firestation::getStation)
                    .isEqualTo("7");
        }

        @Test
        @DisplayName("postFirestation adds new when no address match")
        void post_addsNew() {
            backingList.add(fs("Existing", "1"));

            Firestation toPost = fs("New Address", "5");
            Firestation returned = service.postFirestation(toPost);

            assertThat(backingList).hasSize(2);
            assertThat(backingList).extracting(Firestation::getAddress)
                    .containsExactlyInAnyOrder("Existing", "New Address");
            assertThat(returned).isSameAs(toPost);
        }
    }

    @Nested
    class UpdateFirestation {

        @Test
        @DisplayName("updateFirestation throws on null arguments")
        void update_nullArgs() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.updateFirestation(null, "1"))
                    .withMessage("address must not be null");

            assertThatNullPointerException()
                    .isThrownBy(() -> service.updateFirestation("Addr", null))
                    .withMessage("newStation must not be null");
        }

        @Test
        @DisplayName("updateFirestation throws on blank newStation")
        void update_blankNewStation() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.updateFirestation("Addr", "   "))
                    .withMessage("newStation must not be blank");
        }

        @Test
        @DisplayName("updateFirestation updates only matching address (trim-safe) and returns Optional.of(updated)")
        void update_success() {
            backingList.add(fs(" 1509 Culver St ", "3"));
            backingList.add(fs("Other", "1"));

            Optional<Firestation> updated = service.updateFirestation("1509 Culver St", " 7 ");
            assertThat(updated).isPresent();
            assertThat(updated.get().getStation()).isEqualTo("7"); // trimmed per implementation
            assertThat(backingList.get(0).getStation()).isEqualTo("7");

            // Non-matching
            Optional<Firestation> notFound = service.updateFirestation("Unknown", "9");
            assertThat(notFound).isEmpty();
        }
    }
}
