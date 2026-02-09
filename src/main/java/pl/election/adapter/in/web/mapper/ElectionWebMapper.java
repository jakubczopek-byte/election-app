package pl.election.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import pl.election.adapter.in.web.dto.ElectionResponse;
import pl.election.adapter.in.web.dto.ElectionResultsResponse;
import pl.election.adapter.in.web.dto.OptionResultResponse;
import pl.election.adapter.in.web.dto.VoteResponse;
import pl.election.adapter.in.web.dto.VotingOptionResponse;
import pl.election.application.port.in.ElectionResults;
import pl.election.domain.model.Election;
import pl.election.domain.model.Vote;
import pl.election.domain.model.VotingOption;

@Mapper(componentModel = "spring")
public interface ElectionWebMapper {

    default ElectionResponse toResponse(Election election) {
        var options = election.votingOptions().stream()
                .map(this::toOptionResponse)
                .toList();
        return new ElectionResponse(election.id().value(), election.name(), options, election.createdAt());
    }

    default VotingOptionResponse toOptionResponse(VotingOption option) {
        return new VotingOptionResponse(option.id().value(), option.name());
    }

    default VoteResponse toVoteResponse(Vote vote) {
        return new VoteResponse(
                vote.id().value(),
                vote.voterId().value(),
                vote.electionId().value(),
                vote.votingOptionId().value(),
                vote.castAt());
    }

    default ElectionResultsResponse toResultsResponse(ElectionResults results) {
        var totalVotes = results.results().stream().mapToLong(ElectionResults.OptionResult::voteCount).sum();
        var optionResults = results.results().stream()
                .map(r -> new OptionResultResponse(
                        r.optionId().value(),
                        r.optionName(),
                        r.voteCount(),
                        totalVotes > 0 ? (r.voteCount() * 100.0) / totalVotes : 0.0))
                .toList();
        return new ElectionResultsResponse(results.electionId().value(), results.electionName(), optionResults, totalVotes);
    }
}
