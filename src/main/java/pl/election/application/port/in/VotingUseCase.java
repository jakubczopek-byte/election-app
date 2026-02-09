package pl.election.application.port.in;

import pl.election.domain.model.ElectionId;
import pl.election.domain.model.Vote;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VotingOptionId;

public interface VotingUseCase {

    Vote castVote(VoterId voterId, ElectionId electionId, VotingOptionId votingOptionId);

    ElectionResults getResults(ElectionId electionId);
}
