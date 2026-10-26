-- Notifications for students and stores.
-- The back end creates this table automatically on start-up; run this file
-- yourself only if you see "Could not create the notifications table".
USE smart_cafeteria;

CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_type  VARCHAR(20)  NOT NULL,   -- STUDENT or STORE
    recipient_id    INT          NOT NULL,   -- students.student_id / food_stores.store_id
    event_type      VARCHAR(40)  NOT NULL,   -- ORDER_PLACED, ORDER_CANCELLED, STATUS_Accepted ...
    message         VARCHAR(500) NOT NULL,
    order_id        INT          NULL,
    is_read         TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL,
    INDEX idx_notifications_recipient (recipient_type, recipient_id, is_read),
    UNIQUE KEY uq_notifications_event
        (order_id, recipient_type, recipient_id, event_type)
);
