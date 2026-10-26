package com.eusleats.eusleats.service;

import com.eusleats.eusleats.dto.NotificationResponse;
import com.eusleats.eusleats.entity.Notification;
import com.eusleats.eusleats.entity.Order;
import com.eusleats.eusleats.repository.NotificationRepository;
import com.eusleats.eusleats.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    public static final String STUDENT = "STUDENT";
    public static final String STORE = "STORE";

    private final NotificationRepository notificationRepository;
    private final OrderRepository orderRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            OrderRepository orderRepository) {

        this.notificationRepository = notificationRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Saves one notification in its own transaction, so a problem here
     * cannot undo the order transaction.
     *
     * The same event for the same order and recipient is stored only once.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(
            String recipientType,
            Integer recipientId,
            String eventType,
            Integer orderId,
            String message) {

        if (orderId != null
                && notificationRepository
                .existsByOrderIdAndRecipientTypeAndRecipientIdAndEventType(
                        orderId,
                        recipientType,
                        recipientId,
                        eventType)) {
            return;
        }

        Notification notification = new Notification();
        notification.setRecipientType(recipientType);
        notification.setRecipientId(recipientId);
        notification.setEventType(eventType);
        notification.setOrderId(orderId);
        notification.setMessage(message);
        notification.setSeen(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    public List<NotificationResponse> list(
            String recipientType,
            Integer recipientId) {

        return notificationRepository
                .findTop100ByRecipientTypeAndRecipientIdOrderByCreatedAtDescNotificationIdDesc(
                        recipientType,
                        recipientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long unreadCount(
            String recipientType,
            Integer recipientId) {

        return notificationRepository
                .countByRecipientTypeAndRecipientIdAndSeenFalse(
                        recipientType,
                        recipientId);
    }

    /**
     * Only the notification's intended recipient can mark it as read.
     */
    @Transactional
    public NotificationResponse markRead(
            String recipientType,
            Integer recipientId,
            Integer notificationId) {

        Notification notification = notificationRepository
                .findByNotificationIdAndRecipientTypeAndRecipientId(
                        notificationId,
                        recipientType,
                        recipientId)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));

        notification.setSeen(true);

        Notification saved = notificationRepository.save(notification);

        return toResponse(saved);
    }

    @Transactional
    public int markAllRead(
            String recipientType,
            Integer recipientId) {

        List<Notification> unread = notificationRepository
                .findByRecipientTypeAndRecipientIdAndSeenFalse(
                        recipientType,
                        recipientId);

        for (Notification notification : unread) {
            notification.setSeen(true);
        }

        notificationRepository.saveAll(unread);

        return unread.size();
    }

    /**
     * Returns both:
     * - orderId: the real database ID, used for API operations and links.
     * - displayOrderNumber: the persistent order number shown to users.
     */
    private NotificationResponse toResponse(
            Notification notification) {

        Integer displayOrderNumber = null;

        Integer orderId = notification.getOrderId();

        if (orderId != null) {
            displayOrderNumber = orderRepository
                    .findById(orderId)
                    .map(Order::getDisplayOrderNumber)
                    .orElse(null);
        }

        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getMessage(),
                orderId,
                displayOrderNumber,
                Boolean.TRUE.equals(notification.getSeen()),
                notification.getCreatedAt()
        );
    }

    @Transactional
    public int deleteSelected(
            String recipientType,
            Integer recipientId,
            List<Integer> notificationIds) {

        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }

        List<Notification> selected = notificationRepository
                .findByNotificationIdInAndRecipientTypeAndRecipientId(
                        notificationIds,
                        recipientType,
                        recipientId);

        notificationRepository.deleteAll(selected);

        return selected.size();
    }
}