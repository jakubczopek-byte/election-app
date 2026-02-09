package pl.election.domain.model;

import java.util.UUID;

public record VoterId(UUID value) {

    public static VoterId of(UUID value) { return new VoterId(value); }

    public static VoterId generate() { return new VoterId(UUID.randomUUID()); }
}
