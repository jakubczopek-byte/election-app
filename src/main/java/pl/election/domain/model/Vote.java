package pl.election.domain.model;

import java.time.Instant;

public record Vote(VoteId id, VoterId voterId, ElectionId electionId, VotingOptionId votingOptionId, Instant castAt) {

    public static Vote cast(VoteId id, VoterId voterId, ElectionId electionId, VotingOptionId votingOptionId, Instant castAt) {
        return new Vote(id, voterId, electionId, votingOptionId, castAt);
    }
}
