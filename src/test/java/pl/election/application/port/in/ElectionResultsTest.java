package pl.election.application.port.in;

import org.junit.jupiter.api.Test;
import pl.election.application.port.in.ElectionResults.OptionResult;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.VotingOptionId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElectionResultsTest {

    private static final ElectionId ELECTION_ID = ElectionId.generate();

    @Test
    void should_createResults_when_validData() {
        // given
        var optionId = VotingOptionId.generate();
        var optionResult = new OptionResult(optionId, "Candidate A", 5L);

        // when
        var results = new ElectionResults(ELECTION_ID, "Election 2025", List.of(optionResult));

        // then
        assertThat(results.electionId()).isEqualTo(ELECTION_ID);
        assertThat(results.electionName()).isEqualTo("Election 2025");
        assertThat(results.results()).hasSize(1);
    }

    @Test
    void should_createEmptyResults_when_noOptions() {
        // when
        var results = new ElectionResults(ELECTION_ID, "Election 2025", List.of());

        // then
        assertThat(results.results()).isEmpty();
    }

    @Test
    void should_preserveOptionResult_when_created() {
        // given
        var optionId = VotingOptionId.generate();

        // when
        var optionResult = new OptionResult(optionId, "Candidate A", 42L);

        // then
        assertThat(optionResult)
                .extracting(OptionResult::optionId, OptionResult::optionName, OptionResult::voteCount)
                .containsExactly(optionId, "Candidate A", 42L);
    }

    @Test
    void should_beEqual_when_sameData() {
        // given
        var optionResult = new OptionResult(VotingOptionId.generate(), "Candidate A", 5L);
        var results1 = new ElectionResults(ELECTION_ID, "Election 2025", List.of(optionResult));
        var results2 = new ElectionResults(ELECTION_ID, "Election 2025", List.of(optionResult));

        // then
        assertThat(results1).isEqualTo(results2);
    }

    @Test
    void should_containMultipleOptions_when_multipleProvided() {
        // given
        var optionA = new OptionResult(VotingOptionId.generate(), "Candidate A", 10L);
        var optionB = new OptionResult(VotingOptionId.generate(), "Candidate B", 7L);
        var optionC = new OptionResult(VotingOptionId.generate(), "Candidate C", 3L);

        // when
        var results = new ElectionResults(ELECTION_ID, "Election 2025", List.of(optionA, optionB, optionC));

        // then
        assertThat(results.results())
                .hasSize(3)
                .extracting(OptionResult::voteCount)
                .containsExactly(10L, 7L, 3L);
    }
}
