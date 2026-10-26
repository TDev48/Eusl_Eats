package com.eusleats.eusleats.controller;

import com.eusleats.eusleats.dto.FoodStoreResponse;
import com.eusleats.eusleats.entity.FoodStore;
import com.eusleats.eusleats.repository.FoodStoreRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food-stores")
public class FoodStoreController {

    private final FoodStoreRepository foodStoreRepository;

    public FoodStoreController(
            FoodStoreRepository foodStoreRepository) {

        this.foodStoreRepository = foodStoreRepository;
    }

    @GetMapping
    public List<FoodStoreResponse> getApprovedFoodStores() {

        List<FoodStore> stores =
                foodStoreRepository.findByApprovalStatus(
                        FoodStore.ApprovalStatus.APPROVED
                );

        return stores.stream()
                .map(store -> new FoodStoreResponse(
                        store.getStoreId(),
                        store.getStoreName(),
                        store.getLocation(),
                        store.getDescription(),
                        store.getApprovalStatus().name()
                ))
                .toList();
    }
}