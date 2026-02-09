package pl.election.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ElectionResponse(UUID id, String name, List<VotingOptionResponse> votingOptions, Instant createdAt) {}
