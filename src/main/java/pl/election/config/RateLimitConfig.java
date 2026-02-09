package pl.election.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {

    private long capacity = 100;
    private long refillTokens = 100;
    private Duration refillDuration = Duration.ofSeconds(60);
}
