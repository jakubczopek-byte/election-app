package pl.election.domain.exception;

public class ElectionNotFoundException extends RuntimeException {

    public ElectionNotFoundException(String message) { super(message); }
}
