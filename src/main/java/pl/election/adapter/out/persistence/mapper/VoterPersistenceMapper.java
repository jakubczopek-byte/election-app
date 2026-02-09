package pl.election.adapter.out.persistence.mapper;

import org.mapstruct.Mapper;
import pl.election.adapter.out.persistence.entity.VoterEntity;
import pl.election.domain.model.Voter;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VoterStatus;

@Mapper(componentModel = "spring")
public interface VoterPersistenceMapper {

    default VoterEntity toEntity(Voter voter) {
        return new VoterEntity(
                voter.id().value(),
                voter.name(),
                voter.email(),
                voter.status().name(),
                voter.createdAt());
    }

    default Voter toDomain(VoterEntity entity) {
        return Voter.reconstitute(
                VoterId.of(entity.getId()),
                entity.getName(),
                entity.getEmail(),
                VoterStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt());
    }
}
