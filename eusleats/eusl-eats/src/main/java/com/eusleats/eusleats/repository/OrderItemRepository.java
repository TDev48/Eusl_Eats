
package com.eusleats.eusleats.repository;

import com.eusleats.eusleats.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Integer> {

    List<OrderItem> findByOrderId(Integer orderId);

    boolean existsByFoodId(Integer foodId);
@Query(value = """
    SELECT COUNT(*)
    FROM order_items oi
    INNER JOIN orders o ON o.order_id = oi.order_id
    WHERE oi.food_id = :foodId
      AND UPPER(TRIM(o.status))
          NOT IN ('CANCELLED', 'COMPLETED')
    """, nativeQuery = true)
Long countActiveOrdersForFood(
        @Param("foodId") Integer foodId);

void deleteByOrderId(Integer orderId);
}
