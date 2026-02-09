package pl.election.application.port.out;

import pl.election.domain.model.Election;
import pl.election.domain.model.ElectionId;

import java.util.List;
import java.util.Optional;

public interface ElectionRepository {

    Election save(Election election);

    Optional<Election> findById(ElectionId id);

    List<Election> findAll();
}
