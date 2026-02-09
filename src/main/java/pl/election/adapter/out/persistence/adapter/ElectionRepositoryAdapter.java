package pl.election.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.election.adapter.out.persistence.mapper.ElectionPersistenceMapper;
import pl.election.adapter.out.persistence.repository.SpringElectionRepository;
import pl.election.application.port.out.ElectionRepository;
import pl.election.domain.model.Election;
import pl.election.domain.model.ElectionId;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ElectionRepositoryAdapter implements ElectionRepository {

    private final SpringElectionRepository springRepository;
    private final ElectionPersistenceMapper mapper;

    @Override
    public Election save(Election election) {
        return mapper.toDomain(springRepository.save(mapper.toEntity(election)));
    }

    @Override
    public Optional<Election> findById(ElectionId id) {
        return springRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Election> findAll() {
        return springRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
