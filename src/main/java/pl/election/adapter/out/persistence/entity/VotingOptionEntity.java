package pl.election.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "voting_options")
@Getter
@Setter
@NoArgsConstructor
public class VotingOptionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private ElectionEntity election;

    @Column(nullable = false, length = 200)
    private String name;

    public VotingOptionEntity(UUID id, ElectionEntity election, String name) {
        this.id = id;
        this.election = election;
        this.name = name;
    }
}
