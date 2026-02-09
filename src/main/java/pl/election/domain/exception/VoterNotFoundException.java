package pl.election.domain.exception;

public class VoterNotFoundException extends RuntimeException {

    public VoterNotFoundException(String message) { super(message); }
}
