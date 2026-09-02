package com.starnet.SslAgency.notification.controller;

import com.starnet.SslAgency.notification.service.NotificationService;
import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITMENT_OFFICER','RECEPTIONIST')")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Map<String, Object> getNotifications(@AuthenticationPrincipal Staff actor) {
        Long sid = actor != null ? actor.getId() : null;
        return Map.of(
                "unreadCount", sid != null ? notificationService.getUnreadCountFor(sid) : notificationService.getUnreadCount(),
                "items", sid != null ? notificationService.getUnreadFor(sid) : notificationService.getUnread()
        );
    }

    @PatchMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        notificationService.markRead(id);
    }

    @PatchMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal Staff actor) {
        if (actor != null) notificationService.markAllReadFor(actor.getId());
        else notificationService.markAllRead();
    }
}
