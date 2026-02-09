package pl.election.adapter.out.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.election.application.port.out.MetricsPort;
import pl.election.domain.model.ElectionId;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class MicrometerMetricsAdapter implements MetricsPort {

    private final MeterRegistry registry;

    @Override
    public void recordVoteCast(ElectionId electionId, long durationMs) {
        Timer.builder("votes.cast.duration")
                .tag("electionId", electionId.value().toString())
                .register(registry)
                .record(Duration.ofMillis(durationMs));

        Counter.builder("votes.cast.total")
                .tag("electionId", electionId.value().toString())
                .register(registry)
                .increment();
    }

    @Override
    public void recordResultsQuery(ElectionId electionId, long durationMs) {
        Timer.builder("results.query.duration")
                .tag("electionId", electionId.value().toString())
                .register(registry)
                .record(Duration.ofMillis(durationMs));
    }
}
