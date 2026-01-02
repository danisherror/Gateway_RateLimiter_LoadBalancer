package com.example.gateway.controller;

import com.example.gateway.model.BackendServer;
import com.example.gateway.service.RateLimiterService;
import com.example.gateway.service.RoundRobinService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class GatewayController {

    private final WebClient webClient;
    private final RoundRobinService roundRobinService;
    private final RateLimiterService rateLimiterService;

    public GatewayController(WebClient webClient,
                             RoundRobinService roundRobinService,
                             RateLimiterService rateLimiterService) {
        this.webClient = webClient;
        this.roundRobinService = roundRobinService;
        this.rateLimiterService = rateLimiterService;
    }

    @RequestMapping("/api/**")
    public Mono<ResponseEntity<String>> forward(ServerHttpRequest request,
                                                @RequestBody(required = false) String body) {

        // Use IP or header as key for rate limiting
        String clientKey = request.getRemoteAddress().getAddress().getHostAddress();

        return rateLimiterService.isAllowed(clientKey)
                .flatMap(allowed -> {
                    if (!allowed) {
                        return Mono.just(ResponseEntity.status(429).body("Too many requests"));
                    }

                    BackendServer backend = roundRobinService.getNextHealthyBackend();
                    if (backend == null) {
                        return Mono.just(ResponseEntity.status(503)
                                .body("No healthy backend servers available"));
                    }

                    String path = request.getURI().getPath();
                    String query = request.getURI().getQuery();
                    String targetUrl = backend.getUrl() + path + (query != null ? "?" + query : "");

                    WebClient.RequestBodySpec spec = webClient.method(request.getMethod())
                            .uri(targetUrl)
                            .headers(headers -> headers.addAll(request.getHeaders()));

                    if (body != null) {
                        spec.bodyValue(body);
                    }

                    return spec.retrieve().toEntity(String.class);
                });
    }
}
