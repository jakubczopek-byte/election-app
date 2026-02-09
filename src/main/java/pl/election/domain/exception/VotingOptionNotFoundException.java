package pl.election.domain.exception;

public class VotingOptionNotFoundException extends RuntimeException {

    public VotingOptionNotFoundException(String message) { super(message); }
}
