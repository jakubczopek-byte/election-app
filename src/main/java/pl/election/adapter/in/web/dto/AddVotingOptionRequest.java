package pl.election.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddVotingOptionRequest(@NotBlank @Size(min = 2, max = 200) String name) {}
