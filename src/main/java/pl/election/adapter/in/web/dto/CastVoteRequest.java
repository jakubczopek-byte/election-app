package pl.election.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CastVoteRequest(@NotNull UUID voterId, @NotNull UUID votingOptionId) {}
