package com.example.gateway.service;

import com.example.gateway.config.RateLimiterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final RateLimiterProperties props;

    public RateLimiterService(
            @Qualifier("reactiveRedisTemplate")
            ReactiveRedisTemplate<String, String> redisTemplate,
            RateLimiterProperties props
    ) {
        this.redisTemplate = redisTemplate;
        this.props = props;
    }

    public Mono<Boolean> isAllowed(String key) {
        String tokensKey = "bucket:tokens:" + key;
        String timeKey = "bucket:time:" + key;

        long now = Instant.now().getEpochSecond();

        return redisTemplate.opsForValue().get(timeKey)
                .defaultIfEmpty("0")
                .flatMap(lastTimeStr -> {

                    long lastTime = Long.parseLong(lastTimeStr);
                    long elapsed = Math.max(0, now - lastTime);

                    long tokensToAdd =
                            (elapsed * props.getRefillTokens()) / props.getRefillDuration();

                    return redisTemplate.opsForValue()
                            .get(tokensKey)
                            .defaultIfEmpty(String.valueOf(props.getCapacity()))
                            .flatMap(tokensStr -> {

                                long currentTokens = Long.parseLong(tokensStr);
                                long newTokens = Math.min(
                                        props.getCapacity(),
                                        currentTokens + tokensToAdd
                                );

                                if (newTokens <= 0) {
                                    long retryAfter =
                                            props.getRefillDuration() -
                                                    (elapsed % props.getRefillDuration());

                                    log.warn(
                                            "RATE LIMITED key={} | retry after {} sec",
                                            key,
                                            retryAfter
                                    );

                                    return Mono.just(false);
                                }

                                long remaining = newTokens - 1;

                                log.info(
                                        "Request allowed key={} | remaining tokens={}",
                                        key,
                                        remaining
                                );

                                return redisTemplate.opsForValue()
                                        .set(tokensKey, String.valueOf(remaining))
                                        .then(redisTemplate.opsForValue()
                                                .set(timeKey, String.valueOf(now)))
                                        .thenReturn(true);
                            });
                })
                .doOnError(ex ->
                        log.error("Rate limiter Redis error for key={}", key, ex)
                )
                .onErrorReturn(true); // fail-open
    }
}
