
package com.eusleats.eusleats.repository;

import com.eusleats.eusleats.entity.FoodStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodStoreRepository extends JpaRepository<FoodStore, Integer> {

    List<FoodStore> findByApprovalStatus(
            FoodStore.ApprovalStatus approvalStatus
    );

    FoodStore findByUser_UserId(Integer userId);

    @Query(
        value = "SELECT * FROM food_stores WHERE store_id = :storeId FOR UPDATE",
        nativeQuery = true
    )
    Optional<FoodStore> findByStoreIdForUpdate(
            @Param("storeId") Integer storeId
    );
}
