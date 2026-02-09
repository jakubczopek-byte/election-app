package pl.election.application.service;

import lombok.RequiredArgsConstructor;
import pl.election.application.port.in.VoterUseCase;
import pl.election.application.port.out.ClockPort;
import pl.election.application.port.out.IdGeneratorPort;
import pl.election.application.port.out.VoterRepository;
import pl.election.domain.exception.DuplicateEmailException;
import pl.election.domain.exception.VoterNotFoundException;
import pl.election.domain.model.Voter;
import pl.election.domain.model.VoterId;

import java.util.List;

@RequiredArgsConstructor
public class VoterService implements VoterUseCase {

    private final VoterRepository voterRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    @Override
    public Voter createVoter(String name, String email) {
        if (voterRepository.existsByEmail(email))
            throw new DuplicateEmailException("Email already registered: " + email);
        var id = idGenerator.generateVoterId();
        var now = clock.now();
        return voterRepository.save(Voter.create(id, name, email, now));
    }

    @Override
    public Voter blockVoter(VoterId id) {
        var voter = findOrThrow(id);
        var blockedVoter = voter.block();
        return voterRepository.save(blockedVoter);
    }

    @Override
    public Voter unblockVoter(VoterId id) {
        var voter = findOrThrow(id);
        var unblockedVoter = voter.unblock();
        return voterRepository.save(unblockedVoter);
    }

    @Override
    public Voter getVoter(VoterId id) { return findOrThrow(id); }

    @Override
    public List<Voter> getAllVoters() { return voterRepository.findAll(); }

    private Voter findOrThrow(VoterId id) {
        return voterRepository.findById(id)
                .orElseThrow(() -> new VoterNotFoundException("Voter not found: " + id.value()));
    }
}
