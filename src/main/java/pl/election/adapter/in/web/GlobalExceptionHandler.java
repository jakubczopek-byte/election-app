package pl.election.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.election.adapter.in.web.dto.ApiError;
import pl.election.domain.exception.DuplicateEmailException;
import pl.election.domain.exception.DuplicateVoteException;
import pl.election.domain.exception.ElectionNotFoundException;
import pl.election.domain.exception.VoterBlockedException;
import pl.election.domain.exception.VoterNotFoundException;
import pl.election.domain.exception.VotingOptionNotFoundException;

import java.time.Instant;
import java.util.UUID;

import static java.util.stream.Collectors.joining;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VoterNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle(VoterNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ErrorCode.VOTER_NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ElectionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle(ElectionNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ErrorCode.ELECTION_NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(VotingOptionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle(VotingOptionNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ErrorCode.VOTING_OPTION_NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(VoterBlockedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(VoterBlockedException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ErrorCode.VOTER_BLOCKED, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateVoteException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(DuplicateVoteException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_VOTE, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(DuplicateEmailException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_EMAIL, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handle(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(joining(", "));
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handle(Exception ex, HttpServletRequest request) {
        var correlationId = UUID.randomUUID().toString();
        log.error("Unhandled exception [correlationId={}]", correlationId, ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
                "Internal server error [" + correlationId + "]", request);
    }

    private ApiError buildError(HttpStatus status, ErrorCode code, String message, HttpServletRequest request) {
        return new ApiError(Instant.now(), status.value(), code.name(), message, request.getRequestURI());
    }
}
