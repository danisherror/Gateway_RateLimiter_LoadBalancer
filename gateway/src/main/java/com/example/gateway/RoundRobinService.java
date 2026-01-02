package com.example.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RoundRobinService {

    private final List<String> backends;
    private final AtomicInteger counter = new AtomicInteger(0);

    public RoundRobinService(@Value("${gateway.backend-urls}") String backendUrls) {
        this.backends = List.of(backendUrls.split(","));
    }

    public String getNextBackend() {
        int index = counter.getAndIncrement() % backends.size();
        return backends.get(index);
    }
}
