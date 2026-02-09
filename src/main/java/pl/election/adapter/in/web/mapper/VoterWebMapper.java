package pl.election.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import pl.election.adapter.in.web.dto.VoterResponse;
import pl.election.domain.model.Voter;

@Mapper(componentModel = "spring")
public interface VoterWebMapper {

    default VoterResponse toResponse(Voter voter) {
        return new VoterResponse(
                voter.id().value(),
                voter.name(),
                voter.email(),
                voter.status().name(),
                voter.createdAt());
    }
}
