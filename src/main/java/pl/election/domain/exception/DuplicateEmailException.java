package pl.election.domain.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) { super(message); }
}
