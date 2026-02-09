package pl.election.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.election.adapter.out.persistence.mapper.VotePersistenceMapper;
import pl.election.adapter.out.persistence.repository.SpringVoteRepository;
import pl.election.application.port.out.VoteRepository;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.Vote;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VotingOptionId;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toMap;

@Repository
@RequiredArgsConstructor
public class VoteRepositoryAdapter implements VoteRepository {

    private final SpringVoteRepository springRepository;
    private final VotePersistenceMapper mapper;

    @Override
    public Vote save(Vote vote) {
        return mapper.toDomain(springRepository.save(mapper.toEntity(vote)));
    }

    @Override
    public boolean existsByVoterIdAndElectionId(VoterId voterId, ElectionId electionId) {
        return springRepository.existsByVoterIdAndElectionId(voterId.value(), electionId.value());
    }

    @Override
    public List<Vote> findByElectionId(ElectionId electionId) {
        return springRepository.findByElectionId(electionId.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Map<VotingOptionId, Long> countByElectionIdGroupByOption(ElectionId electionId) {
        return springRepository.countByElectionIdGroupByOption(electionId.value()).stream()
                .collect(toMap(
                        row -> VotingOptionId.of((UUID) row[0]),
                        row -> (Long) row[1]));
    }
}
