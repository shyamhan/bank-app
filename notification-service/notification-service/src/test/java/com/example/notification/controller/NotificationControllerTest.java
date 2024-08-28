package com.example.notification.controller;


import com.example.notification.request.NotificationRequest;
import com.example.notification.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class NotificationControllerTests {

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendNotification_shouldCallEmailService_whenNotificationRequestIsValid() {
        // Arrange
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTo("test@example.com");
        notificationRequest.setSubject("Test Subject");
        notificationRequest.setText("Test Text");

        doNothing().when(emailService).sendNotification(any(String.class), any(String.class), any(String.class));

        // Act
        ResponseEntity<String> response = notificationController.sendNotification(notificationRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(emailService).sendNotification("test@example.com", "Test Subject", "Test Text");
    }
}
