
package com.eusleats.eusleats.repository;

import com.eusleats.eusleats.entity.FoodItemSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodItemSizeRepository
        extends JpaRepository<FoodItemSize, Integer> {

    List<FoodItemSize> findByFoodItem_FoodId(Integer foodId);

    void deleteByFoodItem_FoodId(Integer foodId);
}
