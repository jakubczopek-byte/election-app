package pl.election.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record VoterResponse(UUID id, String name, String email, String status, Instant createdAt) {}
