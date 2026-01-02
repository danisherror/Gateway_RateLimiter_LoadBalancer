package com.example.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private static final int LIMIT = 10;
    private static final Duration WINDOW = Duration.ofSeconds(60); // 1 minute

    public RateLimiterService(
            @Qualifier("reactiveRedisTemplate")
            ReactiveRedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> isAllowed(String key) {
        String redisKey = "rate:" + key;

        log.debug("Rate limit check started for key={}", redisKey);

        return redisTemplate.opsForValue()
                .increment(redisKey)
                .flatMap(count -> {

                    log.info("Rate counter for key={} is {}", redisKey, count);

                    if (count == 1) {
                        log.info("First request for key={}, setting expiry {} seconds",
                                redisKey, WINDOW.getSeconds());

                        return redisTemplate.expire(redisKey, WINDOW)
                                .thenReturn(true);
                    }

                    if (count <= LIMIT) {
                        log.info("Request ALLOWED for key={} (count={}/{})",
                                redisKey, count, LIMIT);
                        return Mono.just(true);
                    }

                    // 🚫 BLOCKED: find retry time
                    return redisTemplate.getExpire(redisKey)
                            .defaultIfEmpty(Duration.ZERO)
                            .map(ttl -> {
                                Instant retryAt = Instant.now().plus(ttl);

                                log.warn(
                                        "Request BLOCKED for key={} (count={}/{}) | Retry after {} seconds at {}",
                                        redisKey,
                                        count,
                                        LIMIT,
                                        ttl.getSeconds(),
                                        retryAt
                                );

                                return false;
                            });
                })
                .doOnError(ex ->
                        log.error("Redis error during rate limiting for key={}", redisKey, ex)
                )
                .onErrorReturn(true); // fail-open
    }
}
