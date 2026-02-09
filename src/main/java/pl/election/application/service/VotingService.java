package pl.election.application.service;

import lombok.RequiredArgsConstructor;
import pl.election.application.port.in.ElectionResults;
import pl.election.application.port.in.ElectionResults.OptionResult;
import pl.election.application.port.in.VotingUseCase;
import pl.election.application.port.out.ClockPort;
import pl.election.application.port.out.ElectionRepository;
import pl.election.application.port.out.IdGeneratorPort;
import pl.election.application.port.out.VoteRepository;
import pl.election.application.port.out.VoterRepository;
import pl.election.domain.exception.DuplicateVoteException;
import pl.election.domain.exception.ElectionNotFoundException;
import pl.election.domain.exception.VoterBlockedException;
import pl.election.domain.exception.VoterNotFoundException;
import pl.election.domain.exception.VotingOptionNotFoundException;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.Vote;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VotingOptionId;

@RequiredArgsConstructor
public class VotingService implements VotingUseCase {

    private final VoterRepository voterRepository;
    private final ElectionRepository electionRepository;
    private final VoteRepository voteRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    @Override
    public Vote castVote(VoterId voterId, ElectionId electionId, VotingOptionId votingOptionId) {
        var voter = voterRepository.findById(voterId)
                .orElseThrow(() -> new VoterNotFoundException("Voter not found: " + voterId.value()));
        if (voter.isBlocked())
            throw new VoterBlockedException("Voter is blocked: " + voterId.value());
        var election = electionRepository.findById(electionId)
                .orElseThrow(() -> new ElectionNotFoundException("Election not found: " + electionId.value()));
        if (!election.hasOption(votingOptionId))
            throw new VotingOptionNotFoundException("Voting option not found: " + votingOptionId.value());
        if (voteRepository.existsByVoterIdAndElectionId(voterId, electionId))
            throw new DuplicateVoteException("Voter already voted in this election");
        var voteId = idGenerator.generateVoteId();
        var now = clock.now();
        return voteRepository.save(Vote.cast(voteId, voterId, electionId, votingOptionId, now));
    }

    @Override
    public ElectionResults getResults(ElectionId electionId) {
        var election = electionRepository.findById(electionId)
                .orElseThrow(() -> new ElectionNotFoundException("Election not found: " + electionId.value()));
        var voteCounts = voteRepository.countByElectionIdGroupByOption(electionId);
        var results = election.votingOptions().stream()
                .map(option -> new OptionResult(
                        option.id(),
                        option.name(),
                        voteCounts.getOrDefault(option.id(), 0L)))
                .toList();
        return new ElectionResults(election.id(), election.name(), results);
    }
}
