package pl.election.domain.model;

import java.time.Instant;
import java.util.regex.Pattern;

import static pl.election.domain.model.VoterStatus.ACTIVE;
import static pl.election.domain.model.VoterStatus.BLOCKED;

public record Voter(
    VoterId id,
    String name,
    String email,
    VoterStatus status,
    Instant createdAt
) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static Voter create(VoterId id, String name, String email, Instant createdAt) {
        var strippedName = name == null ? null : name.strip();
        var strippedEmail = email == null ? null : email.strip();
        validateName(strippedName);
        validateEmail(strippedEmail);
        return new Voter(id, strippedName, strippedEmail, ACTIVE, createdAt);
    }

    public static Voter reconstitute(VoterId id, String name, String email, VoterStatus status, Instant createdAt) {
        return new Voter(id, name, email, status, createdAt);
    }

    public Voter block() {
        return new Voter(id, name, email, BLOCKED, createdAt);
    }

    public Voter unblock() {
        return new Voter(id, name, email, ACTIVE, createdAt);
    }

    public boolean isBlocked() {
        return status == BLOCKED;
    }

    public boolean isActive() {
        return status == ACTIVE;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Voter name must not be blank");
        if (name.strip().length() < 2 || name.strip().length() > 100)
            throw new IllegalArgumentException("Voter name must be between 2 and 100 characters");
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Voter email must not be blank");
        if (!EMAIL_PATTERN.matcher(email).matches())
            throw new IllegalArgumentException("Invalid email format");
    }
}
