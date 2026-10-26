
package com.eusleats.eusleats.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Integer notificationId,
        String message,
        Integer orderId,
        Integer displayOrderNumber,
        boolean read,
        LocalDateTime createdAt
) {
}
