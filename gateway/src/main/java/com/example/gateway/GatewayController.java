package com.example.gateway;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class GatewayController {

    private final WebClient webClient;
    private final RoundRobinService roundRobinService;

    public GatewayController(WebClient webClient, RoundRobinService roundRobinService) {
        this.webClient = webClient;
        this.roundRobinService = roundRobinService;
    }

    @GetMapping("/api/**")
    public Mono<ResponseEntity<String>> forward(ServerHttpRequest request) {
        String backendUrl = roundRobinService.getNextBackend();

        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String targetUrl = backendUrl + path + (query != null ? "?" + query : "");

        return webClient.get()
                .uri(targetUrl)
                .retrieve()
                .toEntity(String.class);
    }
}
