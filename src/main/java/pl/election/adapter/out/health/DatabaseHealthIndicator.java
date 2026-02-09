package pl.election.adapter.out.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (var conn = dataSource.getConnection()) {
            return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("validConnection", true)
                    .build();
        } catch (SQLException e) {
            return Health.down(e)
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
