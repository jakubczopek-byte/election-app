package pl.election.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.election.adapter.out.persistence.entity.ElectionEntity;

import java.util.UUID;

public interface SpringElectionRepository extends JpaRepository<ElectionEntity, UUID> {
}
