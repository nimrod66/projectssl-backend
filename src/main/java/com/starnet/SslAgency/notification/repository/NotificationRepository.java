package com.starnet.SslAgency.notification.repository;

import com.starnet.SslAgency.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReadFalseOrderByCreatedAtDesc();
    long countByReadFalse();

    @Query("select n from Notification n where n.read = false and (n.recipientStaffId is null or n.recipientStaffId = :staffId) order by n.createdAt desc")
    List<Notification> findUnreadVisible(@Param("staffId") Long staffId);

    @Query("select count(n) from Notification n where n.read = false and (n.recipientStaffId is null or n.recipientStaffId = :staffId)")
    long countUnreadVisible(@Param("staffId") Long staffId);

    @Modifying
    @Query("update Notification n set n.read = true where n.read = false and (n.recipientStaffId is null or n.recipientStaffId = :staffId)")
    void markAllReadVisible(@Param("staffId") Long staffId);

    @Modifying
    @Query("update Notification n set n.read = true where n.read = false")
    void markAllReadBulk();
}
