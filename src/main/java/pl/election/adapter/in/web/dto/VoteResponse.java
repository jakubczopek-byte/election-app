package pl.election.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record VoteResponse(UUID id, UUID voterId, UUID electionId, UUID votingOptionId, Instant castAt) {}
