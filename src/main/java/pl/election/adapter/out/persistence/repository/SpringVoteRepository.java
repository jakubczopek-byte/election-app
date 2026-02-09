package pl.election.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.election.adapter.out.persistence.entity.VoteEntity;

import java.util.List;
import java.util.UUID;

public interface SpringVoteRepository extends JpaRepository<VoteEntity, UUID> {

    boolean existsByVoterIdAndElectionId(UUID voterId, UUID electionId);

    List<VoteEntity> findByElectionId(UUID electionId);

    @Query("SELECT v.votingOptionId, COUNT(v) FROM VoteEntity v WHERE v.electionId = :electionId GROUP BY v.votingOptionId")
    List<Object[]> countByElectionIdGroupByOption(UUID electionId);
}
