package pl.election.domain.model;

import java.util.UUID;

public record VotingOptionId(UUID value) {

    public static VotingOptionId of(UUID value) { return new VotingOptionId(value); }

    public static VotingOptionId generate() { return new VotingOptionId(UUID.randomUUID()); }
}
