package pl.election.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.election.adapter.out.persistence.entity.VoterEntity;

import java.util.UUID;

public interface SpringVoterRepository extends JpaRepository<VoterEntity, UUID> {

    boolean existsByEmail(String email);
}
