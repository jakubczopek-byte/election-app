package pl.election.adapter.out.persistence.mapper;

import org.mapstruct.Mapper;
import pl.election.adapter.out.persistence.entity.VoteEntity;
import pl.election.domain.model.ElectionId;
import pl.election.domain.model.Vote;
import pl.election.domain.model.VoteId;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VotingOptionId;

@Mapper(componentModel = "spring")
public interface VotePersistenceMapper {

    default VoteEntity toEntity(Vote vote) {
        return new VoteEntity(
                vote.id().value(),
                vote.voterId().value(),
                vote.electionId().value(),
                vote.votingOptionId().value(),
                vote.castAt());
    }

    default Vote toDomain(VoteEntity entity) {
        return new Vote(
                VoteId.of(entity.getId()),
                VoterId.of(entity.getVoterId()),
                ElectionId.of(entity.getElectionId()),
                VotingOptionId.of(entity.getVotingOptionId()),
                entity.getCastAt());
    }
}
