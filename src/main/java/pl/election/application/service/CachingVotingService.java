package pl.election.application.service;

import lombok.RequiredArgsConstructor;
import pl.election.application.port.in.ElectionResults;
import pl.election.application.port.in.VotingUseCase;
import pl.election.application.port.out.CachePort;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.Vote;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VotingOptionId;

@RequiredArgsConstructor
public class CachingVotingService implements VotingUseCase {

    private final VotingUseCase delegate;
    private final CachePort cachePort;

    @Override
    public Vote castVote(VoterId voterId, ElectionId electionId, VotingOptionId votingOptionId) {
        var vote = delegate.castVote(voterId, electionId, votingOptionId);
        cachePort.evictResults(electionId);
        return vote;
    }

    @Override
    public ElectionResults getResults(ElectionId electionId) {
        return cachePort.getResults(electionId)
                .orElseGet(() -> {
                    var results = delegate.getResults(electionId);
                    cachePort.putResults(electionId, results);
                    return results;
                });
    }
}
