package pl.election.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.election.adapter.out.persistence.mapper.VoterPersistenceMapper;
import pl.election.adapter.out.persistence.repository.SpringVoterRepository;
import pl.election.application.port.out.VoterRepository;
import pl.election.domain.model.Voter;
import pl.election.domain.model.VoterId;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VoterRepositoryAdapter implements VoterRepository {

    private final SpringVoterRepository springRepository;
    private final VoterPersistenceMapper mapper;

    @Override
    public Voter save(Voter voter) {
        return mapper.toDomain(springRepository.save(mapper.toEntity(voter)));
    }

    @Override
    public Optional<Voter> findById(VoterId id) {
        return springRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Voter> findAll() {
        return springRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByEmail(String email) { return springRepository.existsByEmail(email); }
}
