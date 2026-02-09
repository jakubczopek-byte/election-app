package pl.election.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.election.adapter.in.web.RateLimitFilter;
import pl.election.application.port.in.VotingUseCase;
import pl.election.application.port.out.*;
import pl.election.application.service.*;

@Configuration
public class BeanConfig {

    @Bean
    VoterService voterService(VoterRepository voterRepository,
                              IdGeneratorPort idGenerator,
                              ClockPort clock) {
        return new VoterService(voterRepository, idGenerator, clock);
    }

    @Bean
    ElectionService electionService(ElectionRepository electionRepository,
                                     IdGeneratorPort idGenerator,
                                     ClockPort clock) {
        return new ElectionService(electionRepository, idGenerator, clock);
    }

    @Bean
    VotingUseCase votingUseCase(VoterRepository voterRepository,
                                ElectionRepository electionRepository,
                                VoteRepository voteRepository,
                                IdGeneratorPort idGenerator,
                                ClockPort clock,
                                CachePort cachePort,
                                MetricsPort metricsPort) {
        var core = new VotingService(voterRepository, electionRepository, voteRepository, idGenerator, clock);
        var cached = new CachingVotingService(core, cachePort);
        return new ObservableVotingService(cached, metricsPort);
    }

    @Bean
    RateLimitFilter rateLimitFilter(RateLimitConfig config, ObjectMapper objectMapper) {
        return new RateLimitFilter(config.getCapacity(), config.getRefillTokens(),
                config.getRefillDuration(), objectMapper);
    }
}
