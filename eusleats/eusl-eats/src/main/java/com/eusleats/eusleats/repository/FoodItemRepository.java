package com.eusleats.eusleats.repository;

import com.eusleats.eusleats.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodItemRepository
        extends JpaRepository<FoodItem, Integer> {

    List<FoodItem> findByStore_StoreId(Integer storeId);
}