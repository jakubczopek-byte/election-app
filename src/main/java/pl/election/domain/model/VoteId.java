package pl.election.domain.model;

import java.util.UUID;

public record VoteId(UUID value) {

    public static VoteId of(UUID value) { return new VoteId(value); }

    public static VoteId generate() { return new VoteId(UUID.randomUUID()); }
}
