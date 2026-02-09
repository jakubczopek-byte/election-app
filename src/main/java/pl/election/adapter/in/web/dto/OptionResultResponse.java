package pl.election.adapter.in.web.dto;

import java.util.UUID;

public record OptionResultResponse(UUID optionId, String optionName, long voteCount, double percentage) {}
