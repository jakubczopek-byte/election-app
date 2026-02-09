package pl.election.application.service;

import lombok.RequiredArgsConstructor;
import pl.election.application.port.in.ElectionUseCase;
import pl.election.application.port.out.ClockPort;
import pl.election.application.port.out.ElectionRepository;
import pl.election.application.port.out.IdGeneratorPort;
import pl.election.domain.exception.ElectionNotFoundException;
import pl.election.domain.model.Election;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.VotingOption;

import java.util.List;

@RequiredArgsConstructor
public class ElectionService implements ElectionUseCase {

    private final ElectionRepository electionRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    @Override
    public Election createElection(String name) {
        var id = idGenerator.generateElectionId();
        var now = clock.now();
        return electionRepository.save(Election.create(id, name, now));
    }

    @Override
    public VotingOption addVotingOption(ElectionId electionId, String optionName) {
        var election = findOrThrow(electionId);
        var optionId = idGenerator.generateVotingOptionId();
        var option = VotingOption.create(optionId, optionName);
        var updatedElection = election.addVotingOption(option);
        var savedElection = electionRepository.save(updatedElection);
        return savedElection.findOption(option.id())
                .orElseThrow(() -> new IllegalStateException("Option not found after save"));
    }

    @Override
    public Election getElection(ElectionId electionId) { return findOrThrow(electionId); }

    @Override
    public List<Election> getAllElections() { return electionRepository.findAll(); }

    private Election findOrThrow(ElectionId id) {
        return electionRepository.findById(id)
                .orElseThrow(() -> new ElectionNotFoundException("Election not found: " + id.value()));
    }
}
