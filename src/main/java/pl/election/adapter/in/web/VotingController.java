package pl.election.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.election.adapter.in.web.dto.CastVoteRequest;
import pl.election.adapter.in.web.dto.ElectionResultsResponse;
import pl.election.adapter.in.web.dto.VoteResponse;
import pl.election.adapter.in.web.mapper.ElectionWebMapper;
import pl.election.application.port.in.VotingUseCase;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VotingOptionId;

import java.util.UUID;

@Tag(name = "Voting")
@RestController
@RequestMapping("/api/elections/{electionId}")
@RequiredArgsConstructor
public class VotingController {

    private final VotingUseCase votingUseCase;
    private final ElectionWebMapper mapper;

    @Operation(summary = "Cast a vote in an election")
    @ApiResponse(responseCode = "201", description = "Vote cast")
    @PostMapping("/votes")
    @ResponseStatus(HttpStatus.CREATED)
    public VoteResponse castVote(@PathVariable UUID electionId, @Valid @RequestBody CastVoteRequest request) {
        var vote = votingUseCase.castVote(
                VoterId.of(request.voterId()),
                ElectionId.of(electionId),
                VotingOptionId.of(request.votingOptionId()));
        return mapper.toVoteResponse(vote);
    }

    @Operation(summary = "Get election results")
    @GetMapping("/results")
    public ElectionResultsResponse getResults(@PathVariable UUID electionId) {
        return mapper.toResultsResponse(votingUseCase.getResults(ElectionId.of(electionId)));
    }
}
