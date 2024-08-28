package com.example.notification.controller;

import com.example.notification.request.NotificationRequest;
import com.example.notification.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest notificationRequest) {
        emailService.sendNotification(notificationRequest.getTo(), notificationRequest.getSubject(), notificationRequest.getText());
        return ResponseEntity.ok("sent email");
    }
}

