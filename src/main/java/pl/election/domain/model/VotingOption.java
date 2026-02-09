package pl.election.domain.model;

public record VotingOption(
    VotingOptionId id,
    String name
) {

    public static VotingOption create(VotingOptionId id, String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Voting option name must not be blank");
        if (name.strip().length() < 2 || name.strip().length() > 200)
            throw new IllegalArgumentException("Voting option name must be between 2 and 200 characters");
        return new VotingOption(id, name.strip());
    }

    public static VotingOption reconstitute(VotingOptionId id, String name) {
        return new VotingOption(id, name);
    }
}
