package pl.election.adapter.out.persistence.mapper;

import org.mapstruct.Mapper;
import pl.election.adapter.out.persistence.entity.ElectionEntity;
import pl.election.adapter.out.persistence.entity.VotingOptionEntity;
import pl.election.domain.model.Election;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.VotingOption;
import pl.election.domain.model.VotingOptionId;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ElectionPersistenceMapper {

    default ElectionEntity toEntity(Election election) {
        var entity = new ElectionEntity();
        entity.setId(election.id().value());
        entity.setName(election.name());
        entity.setCreatedAt(election.createdAt());
        var options = election.votingOptions().stream()
                .map(o -> new VotingOptionEntity(o.id().value(), entity, o.name()))
                .toList();
        entity.setVotingOptions(new java.util.ArrayList<>(options));
        return entity;
    }

    default Election toDomain(ElectionEntity entity) {
        List<VotingOption> options = entity.getVotingOptions().stream()
                .map(o -> VotingOption.reconstitute(VotingOptionId.of(o.getId()), o.getName()))
                .toList();
        return Election.reconstitute(
                ElectionId.of(entity.getId()),
                entity.getName(),
                options,
                entity.getCreatedAt());
    }
}
