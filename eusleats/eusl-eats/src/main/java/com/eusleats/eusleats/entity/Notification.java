package com.eusleats.eusleats.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A message for a student or a store (for example "New order #12 ...").
 * recipientType is STUDENT or STORE and recipientId is the studentId / storeId.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    @Column(name = "recipient_type")
    private String recipientType;

    @Column(name = "recipient_id")
    private Integer recipientId;

    // What happened, used to avoid duplicates (ORDER_PLACED, ORDER_CANCELLED, STATUS_Accepted ...)
    @Column(name = "event_type")
    private String eventType;

    @Column(name = "message")
    private String message;

    @Column(name = "order_id")
    private Integer orderId;

    // true once the recipient has read it (column is_read)
    @Column(name = "is_read")
    private Boolean seen;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public Integer getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Integer recipientId) {
        this.recipientId = recipientId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
