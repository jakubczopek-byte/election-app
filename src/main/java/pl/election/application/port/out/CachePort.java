package pl.election.application.port.out;

import pl.election.application.port.in.ElectionResults;
import pl.election.domain.model.ElectionId;

import java.util.Optional;

public interface CachePort {
    Optional<ElectionResults> getResults(ElectionId electionId);
    void putResults(ElectionId electionId, ElectionResults results);
    void evictResults(ElectionId electionId);
}
