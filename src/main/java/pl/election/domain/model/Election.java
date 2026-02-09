package pl.election.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Election(
    ElectionId id,
    String name,
    List<VotingOption> votingOptions,
    Instant createdAt
) {

    public Election {
        votingOptions = List.copyOf(votingOptions);
    }

    public static Election create(ElectionId id, String name, Instant createdAt) {
        validateName(name);
        return new Election(id, name.strip(), List.of(), createdAt);
    }

    public static Election reconstitute(ElectionId id, String name, List<VotingOption> options, Instant createdAt) {
        return new Election(id, name, options, createdAt);
    }

    public Election addVotingOption(VotingOption option) {
        var newOptions = new ArrayList<>(votingOptions);
        newOptions.add(option);
        return new Election(id, name, newOptions, createdAt);
    }

    public boolean hasOption(VotingOptionId optionId) {
        return votingOptions.stream().anyMatch(o -> o.id().equals(optionId));
    }

    public Optional<VotingOption> findOption(VotingOptionId optionId) {
        return votingOptions.stream().filter(o -> o.id().equals(optionId)).findFirst();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Election name must not be blank");
        if (name.strip().length() < 2 || name.strip().length() > 200)
            throw new IllegalArgumentException("Election name must be between 2 and 200 characters");
    }
}
