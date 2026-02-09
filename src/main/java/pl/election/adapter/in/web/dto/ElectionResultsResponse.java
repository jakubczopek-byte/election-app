package pl.election.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record ElectionResultsResponse(UUID electionId, String electionName, List<OptionResultResponse> results, long totalVotes) {}
