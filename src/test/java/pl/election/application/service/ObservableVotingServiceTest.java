package pl.election.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.election.application.port.in.ElectionResults;
import pl.election.application.port.in.ElectionResults.OptionResult;
import pl.election.application.port.in.VotingUseCase;
import pl.election.application.port.out.MetricsPort;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.Vote;
import pl.election.domain.model.VoteId;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VotingOptionId;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ObservableVotingServiceTest {

    @Mock
    private VotingUseCase delegate;
    @Mock
    private MetricsPort metricsPort;
    @InjectMocks
    private ObservableVotingService observableService;

    private static final VoterId VOTER_ID = VoterId.generate();
    private static final ElectionId ELECTION_ID = ElectionId.generate();
    private static final VotingOptionId OPTION_ID = VotingOptionId.generate();

    @Test
    void should_delegateCastVote_when_called() {
        // given
        var expectedVote = Vote.cast(VoteId.generate(), VOTER_ID, ELECTION_ID, OPTION_ID, Instant.now());
        given(delegate.castVote(VOTER_ID, ELECTION_ID, OPTION_ID)).willReturn(expectedVote);

        // when
        var result = observableService.castVote(VOTER_ID, ELECTION_ID, OPTION_ID);

        // then
        assertThat(result).isEqualTo(expectedVote);
    }

    @Test
    void should_recordVoteCastMetric_when_voteCast() {
        // given
        var expectedVote = Vote.cast(VoteId.generate(), VOTER_ID, ELECTION_ID, OPTION_ID, Instant.now());
        given(delegate.castVote(VOTER_ID, ELECTION_ID, OPTION_ID)).willReturn(expectedVote);

        // when
        observableService.castVote(VOTER_ID, ELECTION_ID, OPTION_ID);

        // then
        var durationCaptor = ArgumentCaptor.forClass(Long.class);
        then(metricsPort).should().recordVoteCast(eq(ELECTION_ID), durationCaptor.capture());
        assertThat(durationCaptor.getValue()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void should_delegateGetResults_when_called() {
        // given
        var expectedResults = new ElectionResults(ELECTION_ID, "Election", List.of(
                new OptionResult(OPTION_ID, "Option A", 5L)
        ));
        given(delegate.getResults(ELECTION_ID)).willReturn(expectedResults);

        // when
        var result = observableService.getResults(ELECTION_ID);

        // then
        assertThat(result).isEqualTo(expectedResults);
    }

    @Test
    void should_recordResultsQueryMetric_when_resultsQueried() {
        // given
        var expectedResults = new ElectionResults(ELECTION_ID, "Election", List.of());
        given(delegate.getResults(ELECTION_ID)).willReturn(expectedResults);

        // when
        observableService.getResults(ELECTION_ID);

        // then
        then(metricsPort).should().recordResultsQuery(eq(ELECTION_ID), anyLong());
    }

    @Test
    void should_recordNonNegativeDuration_when_voteCast() {
        // given
        var expectedVote = Vote.cast(VoteId.generate(), VOTER_ID, ELECTION_ID, OPTION_ID, Instant.now());
        given(delegate.castVote(VOTER_ID, ELECTION_ID, OPTION_ID)).willReturn(expectedVote);

        // when
        observableService.castVote(VOTER_ID, ELECTION_ID, OPTION_ID);

        // then
        var durationCaptor = ArgumentCaptor.forClass(Long.class);
        then(metricsPort).should().recordVoteCast(eq(ELECTION_ID), durationCaptor.capture());
        assertThat(durationCaptor.getValue()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void should_recordNonNegativeDuration_when_resultsQueried() {
        // given
        var expectedResults = new ElectionResults(ELECTION_ID, "Election", List.of());
        given(delegate.getResults(ELECTION_ID)).willReturn(expectedResults);

        // when
        observableService.getResults(ELECTION_ID);

        // then
        var durationCaptor = ArgumentCaptor.forClass(Long.class);
        then(metricsPort).should().recordResultsQuery(eq(ELECTION_ID), durationCaptor.capture());
        assertThat(durationCaptor.getValue()).isGreaterThanOrEqualTo(0L);
    }
}
