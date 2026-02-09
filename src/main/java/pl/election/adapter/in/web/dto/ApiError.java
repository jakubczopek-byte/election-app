package pl.election.adapter.in.web.dto;

import java.time.Instant;

public record ApiError(Instant timestamp, int status, String errorCode, String message, String path) {}
