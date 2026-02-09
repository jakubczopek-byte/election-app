package pl.election.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.election.adapter.in.web.dto.ApiError;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class RateLimitFilter extends OncePerRequestFilter {

    private final long capacity;
    private final long refillTokens;
    private final Duration refillDuration;
    private final ObjectMapper objectMapper;
    private final Cache<String, Bucket> buckets;

    public RateLimitFilter(long capacity, long refillTokens, Duration refillDuration, ObjectMapper objectMapper) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDuration = refillDuration;
        this.objectMapper = objectMapper;
        this.buckets = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .maximumSize(10_000)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var clientIp = request.getRemoteAddr();
        var bucket = buckets.get(clientIp, this::createBucket);
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            var error = new ApiError(Instant.now(), 429, ErrorCode.RATE_LIMIT_EXCEEDED.name(),
                    "Rate limit exceeded", request.getRequestURI());
            objectMapper.writeValue(response.getWriter(), error);
        }
    }

    private Bucket createBucket(String key) {
        var bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, refillDuration)
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
