package com.example.transaction.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TokenValidationService {

    private final WebClient.Builder webClientBuilder;

    public TokenValidationService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public boolean validateToken(String token) {
        Boolean isValid = webClientBuilder.build()
                .post()
                .uri("http://account-service/api/v1/account/validateToken")
                .bodyValue(token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        return Boolean.TRUE.equals(isValid);
    }
}
