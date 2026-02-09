package pl.election.adapter.out.id;

import org.springframework.stereotype.Component;
import pl.election.application.port.out.IdGeneratorPort;
import pl.election.domain.model.*;

@Component
public class UuidIdGeneratorAdapter implements IdGeneratorPort {
    @Override
    public VoterId generateVoterId() {
        return VoterId.generate();
    }

    @Override
    public ElectionId generateElectionId() {
        return ElectionId.generate();
    }

    @Override
    public VoteId generateVoteId() {
        return VoteId.generate();
    }

    @Override
    public VotingOptionId generateVotingOptionId() {
        return VotingOptionId.generate();
    }
}
