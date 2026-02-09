package pl.election.application.service;

import lombok.RequiredArgsConstructor;
import pl.election.application.port.in.ElectionResults;
import pl.election.application.port.in.VotingUseCase;
import pl.election.application.port.out.MetricsPort;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.Vote;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VotingOptionId;

@RequiredArgsConstructor
public class ObservableVotingService implements VotingUseCase {

    private final VotingUseCase delegate;
    private final MetricsPort metricsPort;

    @Override
    public Vote castVote(VoterId voterId, ElectionId electionId, VotingOptionId votingOptionId) {
        var start = System.currentTimeMillis();
        var vote = delegate.castVote(voterId, electionId, votingOptionId);
        var duration = System.currentTimeMillis() - start;
        metricsPort.recordVoteCast(electionId, duration);
        return vote;
    }

    @Override
    public ElectionResults getResults(ElectionId electionId) {
        var start = System.currentTimeMillis();
        var results = delegate.getResults(electionId);
        var duration = System.currentTimeMillis() - start;
        metricsPort.recordResultsQuery(electionId, duration);
        return results;
    }
}
