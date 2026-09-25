package com.thiagolima.desafio_backend_clube_do_Java.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thiagolima.desafio_backend_clube_do_Java.dto.notification.NotificationResponse;
import com.thiagolima.desafio_backend_clube_do_Java.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @RequestMapping("/list")
    public ResponseEntity<List<NotificationResponse>> listNotifications() {
        return ResponseEntity.ok(notificationService.listNotifications());
    }

    @RequestMapping("/{id}/mark-as-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
