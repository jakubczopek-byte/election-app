package pl.election.application.port.out;

import pl.election.domain.model.Voter;
import pl.election.domain.model.VoterId;

import java.util.List;
import java.util.Optional;

public interface VoterRepository {

    Voter save(Voter voter);

    Optional<Voter> findById(VoterId id);

    List<Voter> findAll();

    boolean existsByEmail(String email);
}
