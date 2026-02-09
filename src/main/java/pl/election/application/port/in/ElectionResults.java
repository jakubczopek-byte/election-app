package pl.election.application.port.in;

import pl.election.domain.model.ElectionId;
import pl.election.domain.model.VotingOptionId;

import java.util.List;

public record ElectionResults(ElectionId electionId, String electionName, List<OptionResult> results) {

    public record OptionResult(VotingOptionId optionId, String optionName, long voteCount) {}
}
