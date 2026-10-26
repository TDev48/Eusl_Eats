package com.eusleats.eusleats.service;

import com.eusleats.eusleats.dto.OrderResponse;
import com.eusleats.eusleats.dto.PlaceOrderRequest;
import com.eusleats.eusleats.entity.FoodItem;
import com.eusleats.eusleats.entity.FoodItemSize;
import com.eusleats.eusleats.entity.FoodStore;
import com.eusleats.eusleats.entity.Order;
import com.eusleats.eusleats.entity.OrderItem;
import com.eusleats.eusleats.entity.Student;
import com.eusleats.eusleats.event.OrderEvent;
import com.eusleats.eusleats.repository.FoodItemRepository;
import com.eusleats.eusleats.repository.FoodItemSizeRepository;
import com.eusleats.eusleats.repository.FoodStoreRepository;
import com.eusleats.eusleats.repository.OrderItemRepository;
import com.eusleats.eusleats.repository.OrderRepository;
import com.eusleats.eusleats.repository.StudentRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrderService {

    private static final int MAX_QUANTITY_PER_ITEM = 99;
    private static final int MAX_DIFFERENT_ITEMS = 30;

    private static final List<String> FLOW = List.of(
            "Pending", "Accepted", "Preparing", "Ready", "Completed"
    );

    private static final String CANCELLED = "Cancelled";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StudentRepository studentRepository;
    private final FoodItemRepository foodItemRepository;
    private final FoodItemSizeRepository foodItemSizeRepository;
    private final FoodStoreRepository foodStoreRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            StudentRepository studentRepository,
            FoodItemRepository foodItemRepository,
            FoodItemSizeRepository foodItemSizeRepository,
            FoodStoreRepository foodStoreRepository,
            ApplicationEventPublisher eventPublisher) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.studentRepository = studentRepository;
        this.foodItemRepository = foodItemRepository;
        this.foodItemSizeRepository = foodItemSizeRepository;
        this.foodStoreRepository = foodStoreRepository;
        this.eventPublisher = eventPublisher;
    }

    // -------------------------------------------------------------
    // Order status flow
    // -------------------------------------------------------------

    static List<String> nextStatuses(String current) {

        if (CANCELLED.equals(current) || "Completed".equals(current)) {
            return List.of();
        }

        int position = FLOW.indexOf(current);

        if (position < 0) {
            return List.of();
        }

        List<String> next = new ArrayList<>(
                FLOW.subList(position + 1, FLOW.size()));

        next.add(CANCELLED);

        return next;
    }

    // A food and its selected size identify an order line.
    // A null size represents a food with no configured sizes.
    private record LineKey(Integer foodId, String selectedSize) {
    }

    private record PricedLine(
            FoodItem food,
            Integer quantity,
            String selectedSize,
            BigDecimal unitPrice) {
    }

    // -------------------------------------------------------------
    // Reading orders
    // -------------------------------------------------------------

    public List<OrderResponse> getOrdersByStore(Integer storeId) {

        return toResponses(
                orderRepository.findByStoreIdAndClearedByStoreFalse(storeId)
        );
    }

    public List<OrderResponse> getOrdersByStudent(Integer studentId) {

        return toResponses(
                orderRepository.findByStudentIdAndClearedByStudentFalse(studentId)
        );
    }

    // -------------------------------------------------------------
    // Display-order-number assignment
    // -------------------------------------------------------------

    /**
     * Returns the smallest positive number not currently used by
     * the store's visible order history.
     *
     * Cleared store orders are excluded. Existing orders are never
     * renumbered, even when their display number is reused.
     *
     * Must be called within the transaction holding the store lock.
     */
    private Integer nextDisplayOrderNumber(Integer storeId) {

        List<Order> visibleOrders =
                orderRepository.findByStoreIdAndClearedByStoreFalse(storeId);

        Set<Integer> usedNumbers = new HashSet<>();

        for (Order existingOrder : visibleOrders) {

            Integer number = existingOrder.getDisplayOrderNumber();

            if (number != null && number > 0) {
                usedNumbers.add(number);
            }
        }

        int candidate = 1;

        while (usedNumbers.contains(candidate)) {
            candidate++;
        }

        return candidate;
    }

    // -------------------------------------------------------------
    // Place an order
    // -------------------------------------------------------------

    @Transactional
    public OrderResponse placeOrder(
            Integer studentId,
            PlaceOrderRequest request) {

        if (studentId == null
                || request == null
                || request.getStoreId() == null
                || request.getItems() == null
                || request.getItems().isEmpty()) {

            throw new RuntimeException(
                    "Please select food items before ordering");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        Integer storeId = request.getStoreId();

        /*
         * Lock the store row until this transaction completes.
         * This serializes number assignment for simultaneous orders
         * placed at the same store.
         */
        FoodStore store = foodStoreRepository
                .findByStoreIdForUpdate(storeId)
                .orElseThrow(() ->
                        new RuntimeException("Food store not found"));

        if (store.getApprovalStatus()
                != FoodStore.ApprovalStatus.APPROVED) {

            throw new RuntimeException(
                    "This store is not accepting orders");
        }

        // Merge only identical food + size combinations.
        Map<LineKey, Integer> quantities = new LinkedHashMap<>();

        for (PlaceOrderRequest.OrderItemRequest line
                : request.getItems()) {

            if (line == null
                    || line.getFoodId() == null
                    || line.getQuantity() == null
                    || line.getQuantity() <= 0
                    || line.getQuantity() > MAX_QUANTITY_PER_ITEM) {

                throw new RuntimeException(
                        "Invalid food item or quantity");
            }

            String selectedSize = line.getSelectedSize();

            if (selectedSize != null) {
                selectedSize = selectedSize.trim();

                if (selectedSize.isEmpty()) {
                    selectedSize = null;
                }
            }

            LineKey key = new LineKey(
                    line.getFoodId(), selectedSize);

            int mergedQuantity = quantities.getOrDefault(key, 0)
                    + line.getQuantity();

            if (mergedQuantity > MAX_QUANTITY_PER_ITEM) {
                throw new RuntimeException(
                        "Maximum quantity per food and size is "
                                + MAX_QUANTITY_PER_ITEM);
            }

            quantities.put(key, mergedQuantity);
        }

        if (quantities.size() > MAX_DIFFERENT_ITEMS) {
            throw new RuntimeException(
                    "Too many different items in one order");
        }

        // Validate every line and calculate prices using database values.
        List<PricedLine> pricedLines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<LineKey, Integer> entry
                : quantities.entrySet()) {

            LineKey key = entry.getKey();
            Integer quantity = entry.getValue();

            FoodItem food = foodItemRepository.findById(key.foodId())
                    .orElseThrow(() ->
                            new RuntimeException("Food item not found"));

            if (!food.getStore().getStoreId().equals(storeId)) {
                throw new RuntimeException(
                        "All food items must belong to the same store");
            }

            if (!Boolean.TRUE.equals(food.getAvailability())) {
                throw new RuntimeException(
                        food.getFoodName() + " is not available");
            }

            List<FoodItemSize> availableSizes =
                    foodItemSizeRepository
                            .findByFoodItem_FoodId(food.getFoodId());

            BigDecimal unitPrice;

            if (availableSizes.isEmpty()) {

                // Foods without configured sizes use their base price.
                if (key.selectedSize() != null) {
                    throw new RuntimeException(
                            food.getFoodName()
                                    + " does not have size options.");
                }

                unitPrice = food.getPrice();

            } else {

                // A food with configured sizes requires a valid selection.
                if (key.selectedSize() == null) {
                    throw new RuntimeException(
                            "Please select a size for "
                                    + food.getFoodName());
                }

                FoodItemSize selectedOption = availableSizes.stream()
                        .filter(size -> size.getSizeName()
                                .equals(key.selectedSize()))
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "The selected size "
                                                + key.selectedSize()
                                                + " is not available for "
                                                + food.getFoodName()));

                unitPrice = selectedOption.getPrice();
            }

            pricedLines.add(new PricedLine(
                    food,
                    quantity,
                    key.selectedSize(),
                    unitPrice
            ));

            total = total.add(
                    unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        // Validate the pickup location before saving the order.
        String orderLocation = request.getOrderLocation();

        if (orderLocation == null || orderLocation.trim().isEmpty()) {
            throw new RuntimeException(
                    "Please enter your pickup location.");
        }

        // Assign the display number while holding the store lock.
        Integer displayNumber = nextDisplayOrderNumber(storeId);

        // Save the order only after all lines have been validated.
        Order order = new Order();

        order.setStudentId(student.getStudentId());
        order.setStoreId(storeId);
        order.setDisplayOrderNumber(displayNumber);
        order.setOrderLocation(orderLocation.trim());
        order.setStatus("Pending");
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        // Store the chosen size and historical unit price.
        for (PricedLine pricedLine : pricedLines) {

            OrderItem orderItem = new OrderItem();

            orderItem.setOrderId(savedOrder.getOrderId());
            orderItem.setFoodId(pricedLine.food().getFoodId());
            orderItem.setFoodName(pricedLine.food().getFoodName());
            orderItem.setQuantity(pricedLine.quantity());
            orderItem.setUnitPrice(pricedLine.unitPrice());
            orderItem.setSelectedSize(pricedLine.selectedSize());

            orderItemRepository.save(orderItem);
        }

        OrderResponse response = toResponse(savedOrder);

        eventPublisher.publishEvent(
                OrderEvent.of(OrderEvent.Type.PLACED, response));

        return response;
    }

    // -------------------------------------------------------------
    // Store changes order status
    // -------------------------------------------------------------

    @Transactional
    public OrderResponse updateOrderStatus(
            Integer orderId,
            Integer storeId,
            String status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (!storeId.equals(order.getStoreId())) {
            throw new RuntimeException(
                    "You cannot update this order");
        }

        if (status == null
                || (!FLOW.contains(status)
                && !CANCELLED.equals(status))) {

            throw new RuntimeException("Invalid order status");
        }

        String current = currentStatus(order);

        if (CANCELLED.equals(current)) {
            throw new RuntimeException(
                    "This order was cancelled and cannot be changed.");
        }

        if (current.equals(status)) {
            throw new RuntimeException(
                    "The order is already " + status + ".");
        }

        if (!nextStatuses(current).contains(status)) {
            throw new RuntimeException(
                    "An order that is " + current
                            + " cannot be changed to " + status + ".");
        }

        order.setStatus(status);

        OrderResponse response =
                toResponse(orderRepository.save(order));

        eventPublisher.publishEvent(
                OrderEvent.of(
                        OrderEvent.Type.STATUS_CHANGED, response));

        return response;
    }

    // -------------------------------------------------------------
    // Student cancels their own order
    // -------------------------------------------------------------

    @Transactional
    public OrderResponse cancelOrderByStudent(
            Integer orderId,
            Integer studentId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (!studentId.equals(order.getStudentId())) {
            throw new RuntimeException(
                    "You cannot cancel this order");
        }

        String current = currentStatus(order);

        if (CANCELLED.equals(current)) {
            throw new RuntimeException(
                    "This order has already been cancelled.");
        }

        if (!"Pending".equals(current)) {
            throw new RuntimeException(
                    "This order is already " + current
                            + " and can no longer be cancelled. "
                            + "Please contact the store.");
        }

        order.setStatus(CANCELLED);

        OrderResponse response =
                toResponse(orderRepository.save(order));

        eventPublisher.publishEvent(
                OrderEvent.of(
                        OrderEvent.Type.CANCELLED_BY_STUDENT,
                        response));

        return response;
    }

    // -------------------------------------------------------------
    // Status and response helpers
    // -------------------------------------------------------------

    private String currentStatus(Order order) {

        String status = order.getStatus();

        if (status == null) {
            return "Pending";
        }

        status = status.trim();

        if ("CANCELLED".equalsIgnoreCase(status)
                || CANCELLED.equalsIgnoreCase(status)) {
            return CANCELLED;
        }

        for (String validStatus : FLOW) {
            if (validStatus.equalsIgnoreCase(status)) {
                return validStatus;
            }
        }

        return "Pending";
    }

    private List<OrderResponse> toResponses(List<Order> orders) {

        List<Order> sorted = new ArrayList<>(orders);

        sorted.sort(
                Comparator.comparing(Order::getOrderId).reversed());

        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : sorted) {
            responses.add(toResponse(order));
        }

        return responses;
    }

    private OrderResponse toResponse(Order order) {

        Student student = studentRepository
                .findById(order.getStudentId())
                .orElse(null);

        String studentName = student != null
                && student.getUser() != null
                ? student.getUser().getFullName()
                : "Unknown student";

        String studentPhone = student != null
                && student.getUser() != null
                ? student.getUser().getPhone()
                : null;

        String storeName = foodStoreRepository
                .findById(order.getStoreId())
                .map(FoodStore::getStoreName)
                .orElse("Unknown store");

        List<OrderResponse.Item> items = new ArrayList<>();

        for (OrderItem line :
                orderItemRepository.findByOrderId(order.getOrderId())) {

            String image = null;

            if (line.getFoodId() != null) {
                image = foodItemRepository
                        .findById(line.getFoodId())
                        .map(FoodItem::getImage)
                        .orElse(null);
            }

            items.add(new OrderResponse.Item(
                    line.getFoodId(),
                    line.getFoodName(),
                    line.getQuantity(),
                    line.getUnitPrice(),
                    line.getUnitPrice().multiply(
                            BigDecimal.valueOf(line.getQuantity())),
                    image,
                    line.getSelectedSize()
            ));
        }

        String status = currentStatus(order);

        return new OrderResponse(
                order.getOrderId(),
                order.getDisplayOrderNumber(),
                order.getStudentId(),
                studentName,
                studentPhone,
                order.getStoreId(),
                storeName,
                order.getTotalAmount(),
                status,
                order.getOrderDate(),
                order.getOrderLocation(),
                nextStatuses(status),
                items
        );
    }

    // -------------------------------------------------------------
    // Clear completed/cancelled orders independently for each role
    // -------------------------------------------------------------

    private void validateClearableOrder(
            Order order,
            Integer expectedOwnerId,
            boolean storeAction) {

        Integer actualOwnerId = storeAction
                ? order.getStoreId()
                : order.getStudentId();

        if (expectedOwnerId == null
                || !expectedOwnerId.equals(actualOwnerId)) {

            System.err.println(
                    "Clear order ownership mismatch: orderId="
                            + order.getOrderId()
                            + ", expectedOwnerId=" + expectedOwnerId
                            + ", actualOwnerId=" + actualOwnerId
                            + ", storeAction=" + storeAction
            );

            throw new RuntimeException(
                    "You cannot clear an order belonging to another "
                            + (storeAction ? "store." : "student.")
            );
        }

        String status = currentStatus(order);

        if (!"Completed".equals(status) && !CANCELLED.equals(status)) {
            throw new RuntimeException(
                    "Only completed or cancelled orders can be cleared. "
                            + "Order #" + order.getOrderId()
                            + " is currently " + status + "."
            );
        }
    }

    @Transactional
    public void deleteSelectedOrders(
            Integer storeId,
            List<Integer> orderIds) {

        if (orderIds == null || orderIds.isEmpty()) {
            throw new RuntimeException(
                    "Please select at least one order."
            );
        }

        List<Integer> uniqueIds = orderIds.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (uniqueIds.isEmpty()) {
            throw new RuntimeException(
                    "No valid orders were selected.");
        }

        List<Order> selectedOrders = new ArrayList<>();

        // Validate every order before changing anything.
        for (Integer orderId : uniqueIds) {

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Order not found: " + orderId
                            ));

            validateClearableOrder(order, storeId, true);
            selectedOrders.add(order);
        }

        // Hide orders from the store only. Do not delete order records.
        for (Order order : selectedOrders) {
            order.setClearedByStore(true);
        }

        orderRepository.saveAll(selectedOrders);
    }

    @Transactional
    public void clearSelectedStudentOrders(
            Integer studentId,
            List<Integer> orderIds) {

        if (orderIds == null || orderIds.isEmpty()) {
            throw new RuntimeException(
                    "Please select at least one order."
            );
        }

        List<Integer> uniqueIds = orderIds.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (uniqueIds.isEmpty()) {
            throw new RuntimeException(
                    "No valid orders were selected.");
        }

        List<Order> selectedOrders = new ArrayList<>();

        // Validate every order before changing anything.
        for (Integer orderId : uniqueIds) {

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Order not found: " + orderId
                            ));

            validateClearableOrder(order, studentId, false);
            selectedOrders.add(order);
        }

        // Hide orders from the student only.
        for (Order order : selectedOrders) {
            order.setClearedByStudent(true);
        }

        orderRepository.saveAll(selectedOrders);
    }
}