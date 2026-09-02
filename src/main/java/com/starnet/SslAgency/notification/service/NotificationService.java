package com.starnet.SslAgency.notification.service;

import com.starnet.SslAgency.notification.model.Notification;
import com.starnet.SslAgency.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void create(String message, String type, Long entityId, String entityType) {
        create(message, type, entityId, entityType, null);
    }

    @Transactional
    public void create(String message, String type, Long entityId, String entityType, Long recipientStaffId) {
        Notification n = Notification.builder()
                .message(message)
                .type(type != null ? Notification.Type.valueOf(type) : null)
                .entityId(entityId)
                .entityType(entityType)
                .recipientStaffId(recipientStaffId)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(n);
    }

    public List<Notification> getUnread() {
        return notificationRepository.findByReadFalseOrderByCreatedAtDesc();
    }

    public long getUnreadCount() {
        return notificationRepository.countByReadFalse();
    }

    public List<Notification> getUnreadFor(Long staffId) {
        return notificationRepository.findUnreadVisible(staffId);
    }

    public long getUnreadCountFor(Long staffId) {
        return notificationRepository.countUnreadVisible(staffId);
    }

    @Transactional
    public void markRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> { n.setRead(true); notificationRepository.save(n); });
    }

    @Transactional
    public void markAllRead() {
        notificationRepository.markAllReadBulk();
    }

    @Transactional
    public void markAllReadFor(Long staffId) {
        notificationRepository.markAllReadVisible(staffId);
    }
}
