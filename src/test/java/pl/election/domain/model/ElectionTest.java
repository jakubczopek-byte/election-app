package pl.election.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElectionTest {

    private static final ElectionId ID = ElectionId.generate();
    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_createElection_when_validName() {
        // when
        var election = Election.create(ID, "Mayor Election 2025", NOW);

        // then
        assertThat(election)
                .extracting(Election::id, Election::name, Election::createdAt)
                .containsExactly(ID, "Mayor Election 2025", NOW);
        assertThat(election.votingOptions()).isEmpty();
    }

    @Test
    void should_stripName_when_hasLeadingTrailingSpaces() {
        // when
        var election = Election.create(ID, "  Mayor Election  ", NOW);

        // then
        assertThat(election.name()).isEqualTo("Mayor Election");
    }

    @Test
    void should_addVotingOption_when_called() {
        // given
        var election = Election.create(ID, "Mayor Election 2025", NOW);
        var option = VotingOption.create(VotingOptionId.generate(), "Candidate A");

        // when
        var updated = election.addVotingOption(option);

        // then
        assertThat(updated.votingOptions()).hasSize(1).containsExactly(option);
    }

    @Test
    void should_preserveExistingOptions_when_addingNew() {
        // given
        var optionA = VotingOption.create(VotingOptionId.generate(), "Candidate A");
        var optionB = VotingOption.create(VotingOptionId.generate(), "Candidate B");
        var election = Election.create(ID, "Mayor Election 2025", NOW)
                .addVotingOption(optionA);

        // when
        var updated = election.addVotingOption(optionB);

        // then
        assertThat(updated.votingOptions()).hasSize(2).containsExactly(optionA, optionB);
    }

    @Test
    void should_notMutateOriginal_when_addingOption() {
        // given
        var election = Election.create(ID, "Mayor Election 2025", NOW);
        var option = VotingOption.create(VotingOptionId.generate(), "Candidate A");

        // when
        election.addVotingOption(option);

        // then - original unchanged
        assertThat(election.votingOptions()).isEmpty();
    }

    @Test
    void should_returnTrue_when_optionExists() {
        // given
        var option = VotingOption.create(VotingOptionId.generate(), "Candidate A");
        var election = Election.create(ID, "Mayor Election 2025", NOW).addVotingOption(option);

        // then
        assertThat(election.hasOption(option.id())).isTrue();
    }

    @Test
    void should_returnFalse_when_optionDoesNotExist() {
        // given
        var election = Election.create(ID, "Mayor Election 2025", NOW);

        // then
        assertThat(election.hasOption(VotingOptionId.generate())).isFalse();
    }

    @Test
    void should_findOption_when_optionExists() {
        // given
        var option = VotingOption.create(VotingOptionId.generate(), "Candidate A");
        var election = Election.create(ID, "Mayor Election 2025", NOW).addVotingOption(option);

        // when
        var found = election.findOption(option.id());

        // then
        assertThat(found).isPresent().hasValue(option);
    }

    @Test
    void should_returnEmpty_when_optionNotFound() {
        // given
        var election = Election.create(ID, "Mayor Election 2025", NOW);

        // when
        var found = election.findOption(VotingOptionId.generate());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void should_returnUnmodifiableList_when_gettingOptions() {
        // given
        var option = VotingOption.create(VotingOptionId.generate(), "Candidate A");
        var election = Election.create(ID, "Mayor Election 2025", NOW).addVotingOption(option);

        // then
        assertThatThrownBy(() -> election.votingOptions().add(VotingOption.create(VotingOptionId.generate(), "Hack")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_returnUnmodifiableList_when_noOptions() {
        // given
        var election = Election.create(ID, "Mayor Election 2025", NOW);

        // then
        assertThatThrownBy(() -> election.votingOptions().add(VotingOption.create(VotingOptionId.generate(), "Hack")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_reconstituteElection_when_calledWithAllFields() {
        // given
        var option = VotingOption.reconstitute(VotingOptionId.generate(), "Candidate A");

        // when
        var election = Election.reconstitute(ID, "Mayor Election 2025", List.of(option), NOW);

        // then
        assertThat(election.id()).isEqualTo(ID);
        assertThat(election.name()).isEqualTo("Mayor Election 2025");
        assertThat(election.votingOptions()).hasSize(1);
        assertThat(election.createdAt()).isEqualTo(NOW);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void should_throwException_when_nameBlank(String name) {
        assertThatThrownBy(() -> Election.create(ID, name, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void should_throwException_when_nameTooShort() {
        assertThatThrownBy(() -> Election.create(ID, "A", NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 2 and 200");
    }

    @Test
    void should_throwException_when_nameTooLong() {
        // given
        var longName = "A".repeat(201);

        // then
        assertThatThrownBy(() -> Election.create(ID, longName, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 2 and 200");
    }

    @Test
    void should_acceptName_when_minLength() {
        // when
        var election = Election.create(ID, "ab", NOW);

        // then
        assertThat(election.name()).isEqualTo("ab");
    }

    @Test
    void should_acceptName_when_exactlyMaxLength() {
        // given
        var maxName = "A".repeat(200);

        // when
        var election = Election.create(ID, maxName, NOW);

        // then
        assertThat(election.name()).isEqualTo(maxName);
    }

    @Test
    void should_findCorrectOption_when_multipleOptionsExist() {
        // given
        var optionA = VotingOption.create(VotingOptionId.generate(), "Candidate A");
        var optionB = VotingOption.create(VotingOptionId.generate(), "Candidate B");
        var optionC = VotingOption.create(VotingOptionId.generate(), "Candidate C");
        var election = Election.create(ID, "Mayor Election 2025", NOW)
                .addVotingOption(optionA)
                .addVotingOption(optionB)
                .addVotingOption(optionC);

        // when
        var found = election.findOption(optionB.id());

        // then
        assertThat(found).isPresent().hasValue(optionB);
    }
}
