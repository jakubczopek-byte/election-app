package pl.election.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.election.domain.model.VoterStatus.ACTIVE;
import static pl.election.domain.model.VoterStatus.BLOCKED;

class VoterTest {

    private static final VoterId ID = VoterId.generate();
    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_createVoter_when_validNameAndEmail() {
        // given
        var name = "Jan Kowalski";
        var email = "jan@example.com";

        // when
        var voter = Voter.create(ID, name, email, NOW);

        // then
        assertThat(voter)
                .extracting(Voter::id, Voter::name, Voter::email, Voter::status, Voter::createdAt)
                .containsExactly(ID, name, email, ACTIVE, NOW);
    }

    @Test
    void should_stripWhitespace_when_nameHasLeadingOrTrailingSpaces() {
        // when
        var voter = Voter.create(ID, "  Jan Kowalski  ", "jan@example.com", NOW);

        // then
        assertThat(voter.name()).isEqualTo("Jan Kowalski");
    }

    @Test
    void should_stripWhitespace_when_emailHasLeadingOrTrailingSpaces() {
        // when
        var voter = Voter.create(ID, "Jan Kowalski", "  jan@example.com  ", NOW);

        // then
        assertThat(voter.email()).isEqualTo("jan@example.com");
    }

    @Test
    void should_createWithActiveStatus_when_created() {
        // when
        var voter = Voter.create(ID, "Jan Kowalski", "jan@example.com", NOW);

        // then
        assertThat(voter.isActive()).isTrue();
        assertThat(voter.isBlocked()).isFalse();
    }

    @Test
    void should_blockVoter_when_active() {
        // given
        var voter = Voter.create(ID, "Jan Kowalski", "jan@example.com", NOW);

        // when
        var blocked = voter.block();

        // then
        assertThat(blocked.isBlocked()).isTrue();
        assertThat(blocked.isActive()).isFalse();
        assertThat(blocked.status()).isEqualTo(BLOCKED);
    }

    @Test
    void should_preserveIdentity_when_blocking() {
        // given
        var voter = Voter.create(ID, "Jan Kowalski", "jan@example.com", NOW);

        // when
        var blocked = voter.block();

        // then
        assertThat(blocked)
                .extracting(Voter::id, Voter::name, Voter::email, Voter::createdAt)
                .containsExactly(voter.id(), voter.name(), voter.email(), voter.createdAt());
    }

    @Test
    void should_unblockVoter_when_blocked() {
        // given
        var blocked = Voter.create(ID, "Jan Kowalski", "jan@example.com", NOW).block();

        // when
        var unblocked = blocked.unblock();

        // then
        assertThat(unblocked.isActive()).isTrue();
        assertThat(unblocked.isBlocked()).isFalse();
    }

    @Test
    void should_remainBlocked_when_alreadyBlocked() {
        // given
        var blocked = Voter.create(ID, "Jan Kowalski", "jan@example.com", NOW).block();

        // when
        var stillBlocked = blocked.block();

        // then
        assertThat(stillBlocked.isBlocked()).isTrue();
    }

    @Test
    void should_remainActive_when_alreadyActive() {
        // given
        var voter = Voter.create(ID, "Jan Kowalski", "jan@example.com", NOW);

        // when
        var stillActive = voter.unblock();

        // then
        assertThat(stillActive.isActive()).isTrue();
    }

    @Test
    void should_returnImmutableVoter_when_blocking() {
        // given
        var voter = Voter.create(ID, "Jan Kowalski", "jan@example.com", NOW);

        // when
        voter.block();

        // then - original unchanged
        assertThat(voter.isActive()).isTrue();
    }

    @Test
    void should_returnImmutableVoter_when_unblocking() {
        // given
        var blocked = Voter.create(ID, "Jan Kowalski", "jan@example.com", NOW).block();

        // when
        blocked.unblock();

        // then - original unchanged
        assertThat(blocked.isBlocked()).isTrue();
    }

    @Test
    void should_reconstituteVoter_when_calledWithAllFields() {
        // when
        var voter = Voter.reconstitute(ID, "Jan Kowalski", "jan@example.com", BLOCKED, NOW);

        // then
        assertThat(voter)
                .extracting(Voter::id, Voter::name, Voter::email, Voter::status, Voter::createdAt)
                .containsExactly(ID, "Jan Kowalski", "jan@example.com", BLOCKED, NOW);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void should_throwException_when_nameBlank(String name) {
        assertThatThrownBy(() -> Voter.create(ID, name, "jan@example.com", NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void should_throwException_when_nameTooShort() {
        assertThatThrownBy(() -> Voter.create(ID, "J", "jan@example.com", NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 2 and 100");
    }

    @Test
    void should_throwException_when_nameTooLong() {
        // given
        var longName = "A".repeat(101);

        // then
        assertThatThrownBy(() -> Voter.create(ID, longName, "jan@example.com", NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 2 and 100");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "Aa"})
    void should_acceptName_when_withinBounds(String name) {
        // when
        var voter = Voter.create(ID, name, "jan@example.com", NOW);

        // then
        assertThat(voter.name()).isEqualTo(name);
    }

    @Test
    void should_acceptName_when_exactlyMaxLength() {
        // given
        var maxName = "A".repeat(100);

        // when
        var voter = Voter.create(ID, maxName, "jan@example.com", NOW);

        // then
        assertThat(voter.name()).isEqualTo(maxName);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void should_throwException_when_emailBlank(String email) {
        assertThatThrownBy(() -> Voter.create(ID, "Jan Kowalski", email, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "no-at-sign", "@missing-local", "missing-domain@", "a@b", "a@.com"})
    void should_throwException_when_emailInvalidFormat(String email) {
        assertThatThrownBy(() -> Voter.create(ID, "Jan Kowalski", email, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email");
    }

    @ParameterizedTest
    @CsvSource({
            "jan@example.com",
            "jan.kowalski@example.com",
            "jan+tag@example.com",
            "jan_kowalski@example.co.uk"
    })
    void should_acceptEmail_when_validFormat(String email) {
        // when
        var voter = Voter.create(ID, "Jan Kowalski", email, NOW);

        // then
        assertThat(voter.email()).isEqualTo(email);
    }
}
