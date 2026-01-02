package com.example.gateway.service;

import com.example.gateway.config.RateLimiterProperties;
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

    private static final Logger log =
            LoggerFactory.getLogger(RateLimiterService.class);

    private final ReactiveRedisTemplate<String, String> redis;
    private final RateLimiterProperties props;

    public RateLimiterService(
            @Qualifier("reactiveRedisTemplate")
            ReactiveRedisTemplate<String, String> redis,
            RateLimiterProperties props
    ) {
        this.redis = redis;
        this.props = props;
    }

    public Mono<Boolean> isAllowed(String key) {

        String tokensKey = "tb:tokens:" + key;
        String timeKey   = "tb:time:" + key;

        long now = System.currentTimeMillis();

        double refillRate =
                (double) props.getRefillTokens() /
                        props.getRefillDuration() / 1000.0;

        return redis.opsForValue()
                .get(timeKey)
                .defaultIfEmpty(String.valueOf(now))
                .flatMap(lastTimeStr -> {

                    long lastTime = Long.parseLong(lastTimeStr);
                    long elapsed = Math.max(0, now - lastTime);

                    return redis.opsForValue()
                            .get(tokensKey)
                            .defaultIfEmpty(String.valueOf(props.getCapacity()))
                            .flatMap(tokensStr -> {

                                double tokens =
                                        Double.parseDouble(tokensStr);

                                double newTokens = Math.min(
                                        props.getCapacity(),
                                        tokens + elapsed * refillRate
                                );

                                if (newTokens < 1) {
                                    long retryAfterMs =
                                            (long) ((1 - newTokens) / refillRate);

                                    log.warn(
                                            "RATE LIMITED key={} | retry after {} ms",
                                            key,
                                            retryAfterMs
                                    );

                                    return Mono.just(false);
                                }

                                double remaining = newTokens - 1;

                                log.info(
                                        "ALLOWED key={} | remaining tokens={}",
                                        key,
                                        String.format("%.2f", remaining)
                                );

                                return redis.opsForValue()
                                        .set(tokensKey, String.valueOf(remaining))
                                        .then(redis.opsForValue()
                                                .set(timeKey, String.valueOf(now)))
                                        .then(redis.expire(
                                                tokensKey,
                                                Duration.ofSeconds(props.getRefillDuration() * 2)
                                        ))
                                        .then(redis.expire(
                                                timeKey,
                                                Duration.ofSeconds(props.getRefillDuration() * 2)
                                        ))
                                        .thenReturn(true);
                            });
                })
                .doOnError(ex ->
                        log.error("Redis error in rate limiter key={}", key, ex)
                )
                .onErrorResume(ex -> {
                    log.error("FAIL-OPEN rate limiter key={}", key);
                    return Mono.just(true);
                });
    }
}
