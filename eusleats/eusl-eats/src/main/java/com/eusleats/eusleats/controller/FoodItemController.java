package com.eusleats.eusleats.controller;

import com.eusleats.eusleats.dto.FoodItemRequest;
import com.eusleats.eusleats.service.FoodItemService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/food-items")
public class FoodItemController {

    private final FoodItemService foodItemService;

    public FoodItemController(FoodItemService foodItemService) {
        this.foodItemService = foodItemService;
    }

    // The store id comes from the login token, never from the request body.
    @PostMapping
    public ResponseEntity<?> addFoodItem(
            @RequestAttribute("auth.refId") Integer authStoreId,
            @RequestBody FoodItemRequest request) {

        try {
            return ResponseEntity.ok(
                    foodItemService.addFoodItem(authStoreId, request));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Public: students browse a store's menu.
    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getStoreFoodItems(@PathVariable Integer storeId) {

        try {
            return ResponseEntity.ok(foodItemService.getFoodItemsByStore(storeId));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
@DeleteMapping("/{foodId}/{storeId}")
public ResponseEntity<?> deleteFoodItem(
        @PathVariable Integer foodId,
        @PathVariable Integer storeId) {

    try {
        String message = foodItemService.deleteFoodItem(foodId, storeId);
        return ResponseEntity.ok(message);

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}


    @PutMapping("/{foodId}/{storeId}")
    public ResponseEntity<?> updateFoodItem(
            @PathVariable Integer foodId,
            @PathVariable Integer storeId,
            @RequestBody FoodItemRequest request) {

        try {
            return ResponseEntity.ok(
                    foodItemService.updateFoodItem(foodId, storeId, request));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/availability/{foodId}/{storeId}")
    public ResponseEntity<?> toggleAvailability(
            @PathVariable Integer foodId,
            @PathVariable Integer storeId) {

        try {
            return ResponseEntity.ok(
                    foodItemService.toggleAvailability(foodId, storeId));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
