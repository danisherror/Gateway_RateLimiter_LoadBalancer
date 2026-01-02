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
    private final String backendUrl;

    public GatewayController(
            WebClient webClient,
            @Value("${gateway.backend-url}") String backendUrl) {
        this.webClient = webClient;
        this.backendUrl = backendUrl;
    }

    @GetMapping("/api/**")
    public Mono<ResponseEntity<String>> forward(ServerHttpRequest request) {

        String path = request.getURI().getPath(); // /api/hello
        String query = request.getURI().getQuery();

        String targetUrl = backendUrl + path +
                (query != null ? "?" + query : "");

        return webClient.get()
                .uri(targetUrl)
                .retrieve()
                .toEntity(String.class);
    }
}
