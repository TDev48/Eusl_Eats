package com.eusleats.eusleats.repository;

import com.eusleats.eusleats.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface NotificationRepository
        extends JpaRepository<Notification, Integer> {

    List<Notification> findTop100ByRecipientTypeAndRecipientIdOrderByCreatedAtDescNotificationIdDesc(
            String recipientType, Integer recipientId);

    long countByRecipientTypeAndRecipientIdAndSeenFalse(
            String recipientType, Integer recipientId);

    List<Notification> findByRecipientTypeAndRecipientIdAndSeenFalse(
            String recipientType, Integer recipientId);

    Optional<Notification> findByNotificationIdAndRecipientTypeAndRecipientId(
            Integer notificationId, String recipientType, Integer recipientId);

    boolean existsByOrderIdAndRecipientTypeAndRecipientIdAndEventType(
            Integer orderId, String recipientType, Integer recipientId, String eventType);
     List<Notification> findByNotificationIdInAndRecipientTypeAndRecipientId(
        Collection<Integer> notificationIds,
        String recipientType,
        Integer recipientId);


}
