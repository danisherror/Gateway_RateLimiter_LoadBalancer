package com.example.gateway.scheduler;

import com.example.gateway.model.BackendServer;
import com.example.gateway.service.HealthChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HealthCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckScheduler.class);

    private final List<BackendServer> backends;
    private final HealthChecker healthChecker;

    public HealthCheckScheduler(List<BackendServer> backends, HealthChecker healthChecker) {
        this.backends = backends;
        this.healthChecker = healthChecker;
    }

    @Scheduled(fixedRateString = "${healthcheck.interval:5000}")
    public void checkAllBackends() {
        for (BackendServer backend : backends) {
            healthChecker.checkBackend(backend);
        }
    }
}
