package pl.election.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.election.adapter.in.web.dto.CreateVoterRequest;
import pl.election.adapter.in.web.dto.VoterResponse;
import pl.election.adapter.in.web.mapper.VoterWebMapper;
import pl.election.application.port.in.VoterUseCase;
import pl.election.domain.model.VoterId;

import java.util.List;
import java.util.UUID;

@Tag(name = "Voters")
@RestController
@RequestMapping("/api/voters")
@RequiredArgsConstructor
public class VoterController {

    private final VoterUseCase voterUseCase;
    private final VoterWebMapper mapper;

    @Operation(summary = "Create a new voter")
    @ApiResponse(responseCode = "201", description = "Voter created")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VoterResponse create(@Valid @RequestBody CreateVoterRequest request) {
        return mapper.toResponse(voterUseCase.createVoter(request.name(), request.email()));
    }

    @Operation(summary = "Get all voters")
    @GetMapping
    public List<VoterResponse> getAll() {
        return voterUseCase.getAllVoters().stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Get voter by ID")
    @GetMapping("/{id}")
    public VoterResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(voterUseCase.getVoter(VoterId.of(id)));
    }

    @Operation(summary = "Block a voter")
    @PatchMapping("/{id}/block")
    public VoterResponse block(@PathVariable UUID id) {
        return mapper.toResponse(voterUseCase.blockVoter(VoterId.of(id)));
    }

    @Operation(summary = "Unblock a voter")
    @PatchMapping("/{id}/unblock")
    public VoterResponse unblock(@PathVariable UUID id) {
        return mapper.toResponse(voterUseCase.unblockVoter(VoterId.of(id)));
    }
}
