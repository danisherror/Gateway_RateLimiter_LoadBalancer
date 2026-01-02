package com.example.gateway.service;

import com.example.gateway.model.BackendServer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class RoundRobinService {

    private final List<BackendServer> backends;
    private final AtomicInteger counter = new AtomicInteger(0);

    public RoundRobinService(List<BackendServer> backends) {
        this.backends = backends;
    }

    public BackendServer getNextHealthyBackend() {
        List<BackendServer> healthy = backends.stream()
                .filter(BackendServer::isHealthy)
                .collect(Collectors.toList());

        if (healthy.isEmpty()) {
            return null; // no healthy backend, handle gracefully
        }

        int index = counter.getAndIncrement() % healthy.size();
        return healthy.get(index);
    }
}
