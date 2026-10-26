package com.eusleats.eusleats.event;

import com.eusleats.eusleats.dto.OrderResponse;

import java.math.BigDecimal;

public record OrderEvent(
        Type type,
        Integer orderId,
        Integer displayOrderNumber,
        Integer studentId,
        Integer storeId,
        String studentName,
        String storeName,
        String status,
        BigDecimal totalAmount,
        int itemCount) {

    public enum Type {
        PLACED,
        CANCELLED_BY_STUDENT,
        STATUS_CHANGED
    }

    public static OrderEvent of(Type type, OrderResponse order) {

        return new OrderEvent(
                type,
                order.orderId(),
                order.displayOrderNumber(),
                order.studentId(),
                order.storeId(),
                order.studentName(),
                order.storeName(),
                order.status(),
                order.totalAmount(),
                order.items() == null ? 0 : order.items().size()
        );
    }
}