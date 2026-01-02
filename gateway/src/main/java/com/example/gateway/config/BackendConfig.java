package com.example.gateway.config;

import com.example.gateway.model.BackendServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class BackendConfig {

    @Value("${gateway.backend-urls}")
    private String backendUrls; // comma separated

    @Bean
    public List<BackendServer> backendServers() {
        return Arrays.stream(backendUrls.split(","))
                .map(String::trim)
                .map(BackendServer::new)
                .collect(Collectors.toList());
    }
}
