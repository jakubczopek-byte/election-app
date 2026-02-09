package pl.election.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.election.application.port.in.ElectionResults;
import pl.election.application.port.in.ElectionResults.OptionResult;
import pl.election.application.port.in.VotingUseCase;
import pl.election.application.port.out.CachePort;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.Vote;
import pl.election.domain.model.VoteId;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VotingOptionId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CachingVotingServiceTest {

    @Mock
    private VotingUseCase delegate;
    @Mock
    private CachePort cachePort;
    @InjectMocks
    private CachingVotingService cachingService;

    private static final VoterId VOTER_ID = VoterId.generate();
    private static final ElectionId ELECTION_ID = ElectionId.generate();
    private static final VotingOptionId OPTION_ID = VotingOptionId.generate();

    @Test
    void should_delegateCastVote_when_called() {
        // given
        var expectedVote = Vote.cast(VoteId.generate(), VOTER_ID, ELECTION_ID, OPTION_ID, Instant.now());
        given(delegate.castVote(VOTER_ID, ELECTION_ID, OPTION_ID)).willReturn(expectedVote);

        // when
        var result = cachingService.castVote(VOTER_ID, ELECTION_ID, OPTION_ID);

        // then
        assertThat(result).isEqualTo(expectedVote);
        then(delegate).should().castVote(VOTER_ID, ELECTION_ID, OPTION_ID);
    }

    @Test
    void should_evictCache_when_voteIsCast() {
        // given
        var expectedVote = Vote.cast(VoteId.generate(), VOTER_ID, ELECTION_ID, OPTION_ID, Instant.now());
        given(delegate.castVote(VOTER_ID, ELECTION_ID, OPTION_ID)).willReturn(expectedVote);

        // when
        cachingService.castVote(VOTER_ID, ELECTION_ID, OPTION_ID);

        // then
        then(cachePort).should().evictResults(ELECTION_ID);
    }

    @Test
    void should_returnCachedResults_when_cacheHit() {
        // given
        var cachedResults = new ElectionResults(ELECTION_ID, "Election", List.of(
                new OptionResult(OPTION_ID, "Option A", 5L)
        ));
        given(cachePort.getResults(ELECTION_ID)).willReturn(Optional.of(cachedResults));

        // when
        var result = cachingService.getResults(ELECTION_ID);

        // then
        assertThat(result).isEqualTo(cachedResults);
        then(delegate).should(never()).getResults(ELECTION_ID);
    }

    @Test
    void should_delegateAndCache_when_cacheMiss() {
        // given
        var freshResults = new ElectionResults(ELECTION_ID, "Election", List.of(
                new OptionResult(OPTION_ID, "Option A", 5L)
        ));
        given(cachePort.getResults(ELECTION_ID)).willReturn(Optional.empty());
        given(delegate.getResults(ELECTION_ID)).willReturn(freshResults);

        // when
        var result = cachingService.getResults(ELECTION_ID);

        // then
        assertThat(result).isEqualTo(freshResults);
        then(delegate).should().getResults(ELECTION_ID);
        then(cachePort).should().putResults(ELECTION_ID, freshResults);
    }

    @Test
    void should_notPutInCache_when_cacheHit() {
        // given
        var cachedResults = new ElectionResults(ELECTION_ID, "Election", List.of());
        given(cachePort.getResults(ELECTION_ID)).willReturn(Optional.of(cachedResults));

        // when
        cachingService.getResults(ELECTION_ID);

        // then
        then(cachePort).should(never()).putResults(ELECTION_ID, cachedResults);
    }
}
