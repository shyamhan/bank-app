package com.example.transaction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TokenValidationService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    public boolean validateToken(String token) {
        String url = "http://ACCOUNT-SERVICE/api/v1/account/validateToken";

        Boolean isValid = webClientBuilder.build()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block(); // Blocking here for simplicity

        return Boolean.TRUE.equals(isValid);
    }
}
