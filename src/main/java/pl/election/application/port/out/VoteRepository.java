package pl.election.application.port.out;

import pl.election.domain.model.ElectionId;
import pl.election.domain.model.Vote;
import pl.election.domain.model.VoterId;
import pl.election.domain.model.VotingOptionId;

import java.util.List;
import java.util.Map;

public interface VoteRepository {

    Vote save(Vote vote);

    boolean existsByVoterIdAndElectionId(VoterId voterId, ElectionId electionId);

    List<Vote> findByElectionId(ElectionId electionId);

    Map<VotingOptionId, Long> countByElectionIdGroupByOption(ElectionId electionId);
}
