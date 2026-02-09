package pl.election.domain.exception;

public class DuplicateVoteException extends RuntimeException {

    public DuplicateVoteException(String message) { super(message); }
}
