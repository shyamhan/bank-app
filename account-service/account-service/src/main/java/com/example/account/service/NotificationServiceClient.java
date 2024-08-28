package com.example.account.service;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class NotificationServiceClient {


    private final WebClient webClient;

    public NotificationServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://NOTIFICATION-SERVICE").build();
    }

    public void sendNotification(String to, String subject, String text) {
        NotificationRequest request = new NotificationRequest(to, subject, text);
        webClient.post()
                .uri("/api/v1/notifications/send")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }


    @Getter
    @Setter
    public static class NotificationRequest {
        @JsonProperty("to")
        private final String to;
        @JsonProperty("subject")
        private final String subject;
        @JsonProperty("text")
        private final String text;

        public NotificationRequest(String to, String subject, String text) {
            this.to = to;
            this.subject = subject;
            this.text = text;
        }

        // Getters and setters (optional)
    }
}

