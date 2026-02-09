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
import pl.election.adapter.in.web.dto.AddVotingOptionRequest;
import pl.election.adapter.in.web.dto.ElectionResponse;
import pl.election.adapter.in.web.dto.VotingOptionResponse;
import pl.election.adapter.in.web.mapper.ElectionWebMapper;
import pl.election.application.port.in.ElectionUseCase;
import pl.election.domain.model.ElectionId;

import java.util.List;
import java.util.UUID;

@Tag(name = "Elections")
@RestController
@RequestMapping("/api/elections")
@RequiredArgsConstructor
public class ElectionController {

    private final ElectionUseCase electionUseCase;
    private final ElectionWebMapper mapper;

    @Operation(summary = "Create a new election")
    @ApiResponse(responseCode = "201", description = "Election created")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ElectionResponse create(@Valid @RequestBody pl.election.adapter.in.web.dto.CreateElectionRequest request) {
        return mapper.toResponse(electionUseCase.createElection(request.name()));
    }

    @Operation(summary = "Get all elections")
    @GetMapping
    public List<ElectionResponse> getAll() {
        return electionUseCase.getAllElections().stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Get election by ID")
    @GetMapping("/{id}")
    public ElectionResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(electionUseCase.getElection(ElectionId.of(id)));
    }

    @Operation(summary = "Add a voting option to an election")
    @ApiResponse(responseCode = "201", description = "Option added")
    @PostMapping("/{id}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public VotingOptionResponse addOption(@PathVariable UUID id, @Valid @RequestBody AddVotingOptionRequest request) {
        return mapper.toOptionResponse(electionUseCase.addVotingOption(ElectionId.of(id), request.name()));
    }
}
