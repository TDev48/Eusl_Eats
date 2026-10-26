
package com.eusleats.eusleats.service;

import com.eusleats.eusleats.dto.FoodItemRequest;
import com.eusleats.eusleats.dto.FoodItemResponse;
import com.eusleats.eusleats.entity.FoodItem;
import com.eusleats.eusleats.entity.FoodItemSize;
import com.eusleats.eusleats.entity.FoodStore;
import com.eusleats.eusleats.repository.FoodItemRepository;
import com.eusleats.eusleats.repository.FoodItemSizeRepository;
import com.eusleats.eusleats.repository.FoodStoreRepository;
import com.eusleats.eusleats.repository.OrderItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final FoodItemSizeRepository foodItemSizeRepository;
    private final FoodStoreRepository foodStoreRepository;
    private final OrderItemRepository orderItemRepository;

    public FoodItemService(
            FoodItemRepository foodItemRepository,
            FoodItemSizeRepository foodItemSizeRepository,
            FoodStoreRepository foodStoreRepository,
            OrderItemRepository orderItemRepository) {

        this.foodItemRepository = foodItemRepository;
        this.foodItemSizeRepository = foodItemSizeRepository;
        this.foodStoreRepository = foodStoreRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // -------------------------------------------------------------
    // Add food item
    // -------------------------------------------------------------

    @Transactional
    public FoodItemResponse addFoodItem(
            Integer storeId,
            FoodItemRequest request) {

        validate(request);

        FoodStore store = foodStoreRepository.findById(storeId)
                .orElseThrow(() ->
                        new RuntimeException("Food store not found"));

        if (store.getApprovalStatus()
                != FoodStore.ApprovalStatus.APPROVED) {

            throw new RuntimeException(
                    "Your store is not approved yet. "
                    + "You can add food after the admin approves it.");
        }

        FoodItem foodItem = new FoodItem();

        foodItem.setStore(store);
        foodItem.setCategoryId(request.getCategoryId());
        foodItem.setFoodName(request.getFoodName().trim());
        foodItem.setDescription(request.getDescription());
        foodItem.setPrice(request.getPrice());
        foodItem.setImage(request.getImage());
        foodItem.setCreatedAt(LocalDateTime.now());
        foodItem.setAvailability(
                request.getAvailability() == null
                || request.getAvailability());

        FoodItem savedFood = foodItemRepository.save(foodItem);

        saveSizes(savedFood, request.getSizes());

        return toResponse(savedFood);
    }

    // -------------------------------------------------------------
    // Get food items for a store
    // -------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<FoodItemResponse> getFoodItemsByStore(
            Integer storeId) {

        return foodItemRepository
                .findByStore_StoreId(storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // -------------------------------------------------------------
    // Delete food item
    // -------------------------------------------------------------

    @Transactional
    public String deleteFoodItem(
            Integer foodId,
            Integer storeId) {

        FoodItem foodItem =
                findOwnedItem(foodId, storeId, "delete");

        Long activeOrders =
                orderItemRepository.countActiveOrdersForFood(foodId);

        if (activeOrders != null && activeOrders > 0) {
            throw new RuntimeException(
                    "This item cannot be deleted because "
                    + "it has ongoing orders.");
        }
        foodItemSizeRepository.deleteByFoodItem_FoodId(foodId);

        foodItemRepository.delete(foodItem);

        return "Food item deleted successfully.";
    }

    // -------------------------------------------------------------
    // Update food item
    // -------------------------------------------------------------

    @Transactional
    public FoodItemResponse updateFoodItem(
            Integer foodId,
            Integer storeId,
            FoodItemRequest request) {

        validate(request);

        FoodItem foodItem =
                findOwnedItem(foodId, storeId, "edit");

        foodItem.setCategoryId(request.getCategoryId());
        foodItem.setFoodName(request.getFoodName().trim());
        foodItem.setDescription(request.getDescription());
        foodItem.setPrice(request.getPrice());
        foodItem.setImage(request.getImage());

        if (request.getAvailability() != null) {
            foodItem.setAvailability(request.getAvailability());
        }

        FoodItem savedFood = foodItemRepository.save(foodItem);

        saveSizes(savedFood, request.getSizes());

        return toResponse(savedFood);
    }

    // -------------------------------------------------------------
    // Toggle availability
    // -------------------------------------------------------------

    @Transactional
    public FoodItemResponse toggleAvailability(
            Integer foodId,
            Integer storeId) {

        FoodItem foodItem =
                findOwnedItem(foodId, storeId, "change");

        foodItem.setAvailability(
                !Boolean.TRUE.equals(foodItem.getAvailability()));

        return toResponse(foodItemRepository.save(foodItem));
    }

    // -------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------

    private FoodItem findOwnedItem(
            Integer foodId,
            Integer storeId,
            String action) {

        FoodItem foodItem = foodItemRepository.findById(foodId)
                .orElseThrow(() ->
                        new RuntimeException("Food item not found"));

        if (!foodItem.getStore().getStoreId().equals(storeId)) {
            throw new RuntimeException(
                    "You cannot " + action + " this food item");
        }

        return foodItem;
    }

    private void validate(FoodItemRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Food item details are required.");
        }

        if (request.getFoodName() == null
                || request.getFoodName().isBlank()) {

            throw new IllegalArgumentException(
                    "Food name is required");
        }

        if (request.getPrice() == null
                || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Price must be greater than zero");
        }

        if (request.getCategoryId() == null
                || request.getCategoryId() < 1
                || request.getCategoryId() > 6) {

            throw new IllegalArgumentException(
                    "Please choose a valid category");
        }

        validateSizes(request.getSizes());
    }

    private void validateSizes(
            List<FoodItemRequest.FoodSizeRequest> sizes) {

        if (sizes == null) {
            return;
        }

        Set<String> allowedSizes =
                Set.of("Small", "Medium", "Large");

        Set<String> usedSizes = new HashSet<>();

        for (FoodItemRequest.FoodSizeRequest size : sizes) {

            if (size == null || size.getSizeName() == null) {
                throw new IllegalArgumentException(
                        "Each size must have a valid size name.");
            }

            String sizeName = size.getSizeName().trim();

            if (!allowedSizes.contains(sizeName)) {
                throw new IllegalArgumentException(
                        "Size must be Small, Medium, or Large.");
            }

            if (!usedSizes.add(sizeName)) {
                throw new IllegalArgumentException(
                        "Duplicate size options are not allowed.");
            }

            if (size.getPrice() == null
                    || size.getPrice().signum() <= 0
                    || size.getPrice().scale() > 2) {

                throw new IllegalArgumentException(
                        "Each size must have a positive price "
                        + "with at most two decimal places.");
            }
        }
    }

    private void saveSizes(
            FoodItem food,
            List<FoodItemRequest.FoodSizeRequest> sizes) {

        // Older clients may omit the sizes field.
        // In that case, preserve the existing size options.
        if (sizes == null) {
            return;
        }

        // Validate before deleting anything.
        validateSizes(sizes);

        // An empty list intentionally removes all size options.
        foodItemSizeRepository.deleteByFoodItem_FoodId(
        food.getFoodId());

// Ensure existing size rows are deleted before inserting replacements.
foodItemSizeRepository.flush();

for (FoodItemRequest.FoodSizeRequest request : sizes) {

            FoodItemSize size = new FoodItemSize();

            size.setFoodItem(food);
            size.setSizeName(request.getSizeName().trim());
            size.setPrice(request.getPrice());

            foodItemSizeRepository.save(size);
        }
    }

    // Never return the entity itself.
    private FoodItemResponse toResponse(FoodItem foodItem) {

        FoodItemResponse response = new FoodItemResponse(
                foodItem.getFoodId(),
                foodItem.getStore().getStoreId(),
                foodItem.getCategoryId(),
                foodItem.getFoodName(),
                foodItem.getDescription(),
                foodItem.getPrice(),
                foodItem.getImage(),
                foodItem.getAvailability()
        );

        response.setSizes(
                getSizeResponses(foodItem.getFoodId()));

        return response;
    }

    private List<FoodItemResponse.FoodSizeResponse> getSizeResponses(
            Integer foodId) {

        List<FoodItemSize> savedSizes =
                foodItemSizeRepository
                        .findByFoodItem_FoodId(foodId);

        List<FoodItemResponse.FoodSizeResponse> responses =
                new ArrayList<>();

        for (FoodItemSize size : savedSizes) {
            responses.add(
                    new FoodItemResponse.FoodSizeResponse(
                            size.getSizeName(),
                            size.getPrice())
            );
        }

        return responses;
    }
}
