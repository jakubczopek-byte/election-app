package pl.election.adapter.out.time;

import org.springframework.stereotype.Component;
import pl.election.application.port.out.ClockPort;

import java.time.Instant;

@Component
public class SystemClockAdapter implements ClockPort {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
