package pl.election.application.port.out;

import pl.election.domain.model.*;

public interface IdGeneratorPort {
    VoterId generateVoterId();
    ElectionId generateElectionId();
    VoteId generateVoteId();
    VotingOptionId generateVotingOptionId();
}
