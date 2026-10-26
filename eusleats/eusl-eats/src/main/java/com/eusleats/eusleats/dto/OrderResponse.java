
package com.eusleats.eusleats.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Integer orderId,
        Integer displayOrderNumber,
        Integer studentId,
        String studentName,
        String studentPhone,
        Integer storeId,
        String storeName,
        BigDecimal totalAmount,
        String status,
        LocalDateTime orderDate,
        String orderLocation,
        List<String> nextStatuses,
        List<Item> items
) {
    public record Item(
            Integer foodId,
            String foodName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            String image,
            String selectedSize
    ) {
    }
}
