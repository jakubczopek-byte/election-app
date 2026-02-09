package pl.election.adapter.out.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;
import pl.election.application.port.in.ElectionResults;
import pl.election.application.port.out.CachePort;
import pl.election.domain.model.ElectionId;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SpringCacheAdapter implements CachePort {

    private final CacheManager cacheManager;
    private static final String CACHE_NAME = "election-results";

    @Override
    public Optional<ElectionResults> getResults(ElectionId electionId) {
        return Optional.ofNullable(cacheManager.getCache(CACHE_NAME))
                .map(cache -> cache.get(electionId.value(), ElectionResults.class));
    }

    @Override
    public void putResults(ElectionId electionId, ElectionResults results) {
        Optional.ofNullable(cacheManager.getCache(CACHE_NAME))
                .ifPresent(cache -> cache.put(electionId.value(), results));
    }

    @Override
    public void evictResults(ElectionId electionId) {
        Optional.ofNullable(cacheManager.getCache(CACHE_NAME))
                .ifPresent(cache -> cache.evict(electionId.value()));
    }
}
