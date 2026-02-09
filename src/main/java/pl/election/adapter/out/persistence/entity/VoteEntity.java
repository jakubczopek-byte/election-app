package pl.election.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "votes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoteEntity {

    @Id
    private UUID id;

    @Column(name = "voter_id", nullable = false)
    private UUID voterId;

    @Column(name = "election_id", nullable = false)
    private UUID electionId;

    @Column(name = "voting_option_id", nullable = false)
    private UUID votingOptionId;

    @Column(name = "cast_at", nullable = false)
    private Instant castAt;
}
