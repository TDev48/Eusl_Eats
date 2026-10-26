
package com.eusleats.eusleats.repository;

import com.eusleats.eusleats.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Integer> {

    List<Order> findByStoreId(Integer storeId);
    List<Order> findByStudentId(Integer studentId);
    List<Order> findByStoreIdAndClearedByStoreFalse(Integer storeId);
    List<Order> findByStudentIdAndClearedByStudentFalse(Integer studentId);
}
