package com.eusleats.eusleats.controller;

import com.eusleats.eusleats.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

/**
 * Notifications of the logged-in student or store.
 * Who the caller is comes from the login token, so nobody can read or change
 * another account's notifications.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestAttribute("auth.role") String role,
            @RequestAttribute("auth.refId") Integer refId) {

        return ResponseEntity.ok(notificationService.list(role, refId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(
            @RequestAttribute("auth.role") String role,
            @RequestAttribute("auth.refId") Integer refId) {

        return ResponseEntity.ok(
                Map.of("unread", notificationService.unreadCount(role, refId)));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markRead(
            @RequestAttribute("auth.role") String role,
            @RequestAttribute("auth.refId") Integer refId,
            @PathVariable Integer notificationId) {

        try {
            return ResponseEntity.ok(
                    notificationService.markRead(role, refId, notificationId));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllRead(
            @RequestAttribute("auth.role") String role,
            @RequestAttribute("auth.refId") Integer refId) {

        return ResponseEntity.ok(
                Map.of("updated", notificationService.markAllRead(role, refId)));
    }
    @DeleteMapping("/selected")
public ResponseEntity<?> deleteSelected(
        @RequestAttribute("auth.role") String role,
        @RequestAttribute("auth.refId") Integer refId,
        @RequestBody List<Integer> notificationIds) {

    return ResponseEntity.ok(
            Map.of("deleted",
                    notificationService.deleteSelected(
                            role, refId, notificationIds)));
}
}
