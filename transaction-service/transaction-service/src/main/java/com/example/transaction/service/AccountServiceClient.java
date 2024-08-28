package com.example.transaction.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Service
public class AccountServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("http://ACCOUNT-SERVICE")
    private String accountServiceUrl;

    public AccountServiceClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public boolean prepareWithdraw(Long accountId, BigDecimal amount, Long transactionId, String token) {
        return webClientBuilder.build()
                .post()
                .uri(accountServiceUrl + "/api/v1/account/prepare/withdraw/" + accountId+"/"+transactionId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(amount)
                .retrieve()
                .bodyToMono(String.class)
                .block()
                .equals("PREPARED");
    }

    public void commitWithdraw(Long transactionId, String token) {
        webClientBuilder.build()
                .post()
                .uri(accountServiceUrl + "/api/v1/account/commit/withdraw/" + transactionId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void rollbackWithdraw(Long transactionId, String token) {
        webClientBuilder.build()
                .post()
                .uri(accountServiceUrl + "/api/v1/account/rollback/withdraw/" + transactionId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public boolean prepareDeposit(Long accountId, BigDecimal amount, Long transactionId, String token) {
        return webClientBuilder.build()
                .post()
                .uri(accountServiceUrl + "/api/v1/account/prepare/deposit/" + accountId+"/"+transactionId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(amount)
                .retrieve()
                .bodyToMono(String.class)
                .block()
                .equals("PREPARED");
    }

    public void commitDeposit(Long transactionId, String token) {
        webClientBuilder.build()
                .post()
                .uri(accountServiceUrl + "/api/v1/account/commit/deposit/" + transactionId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void rollbackDeposit(Long transactionId, String token) {
        webClientBuilder.build()
                .post()
                .uri(accountServiceUrl + "/api/v1/account/rollback/deposit/" + transactionId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}

