package com.example.gateway.service;

import com.example.gateway.model.BackendServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class HealthChecker {

    private final WebClient webClient;
    private final int failureThreshold;
    private final int successThreshold;

    public HealthChecker(WebClient webClient,
                         @Value("${healthcheck.failure-threshold:3}") int failureThreshold,
                         @Value("${healthcheck.success-threshold:2}") int successThreshold) {
        this.webClient = webClient;
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
    }

    public void checkBackend(BackendServer backend) {
        System.out.println(backend.getUrl());
        webClient.get()
                .uri(backend.getUrl() + "/health")
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(1))
                .subscribe(
                        resp -> onSuccess(backend),
                        err -> onFailure(backend)
                );
    }

    private void onSuccess(BackendServer backend) {
        backend.setConsecutiveFailures(0);
        backend.setConsecutiveSuccesses(backend.getConsecutiveSuccesses() + 1);
        if (backend.getConsecutiveSuccesses() >= successThreshold) {
            backend.setHealthy(true);
        }
    }

    private void onFailure(BackendServer backend) {
        backend.setConsecutiveSuccesses(0);
        backend.setConsecutiveFailures(backend.getConsecutiveFailures() + 1);
        if (backend.getConsecutiveFailures() >= failureThreshold) {
            backend.setHealthy(false);
        }
    }
}
