package pl.election.application.port.out;

import pl.election.domain.model.ElectionId;

public interface MetricsPort {
    void recordVoteCast(ElectionId electionId, long durationMs);
    void recordResultsQuery(ElectionId electionId, long durationMs);
}
