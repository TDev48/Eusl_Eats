package com.eusleats.eusleats.controller;

import com.eusleats.eusleats.dto.OrderResponse;
import com.eusleats.eusleats.dto.PlaceOrderRequest;
import com.eusleats.eusleats.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStore(
            @PathVariable Integer storeId) {

        return ResponseEntity.ok(orderService.getOrdersByStore(storeId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStudent(
            @PathVariable Integer studentId) {

        return ResponseEntity.ok(orderService.getOrdersByStudent(studentId));
    }

    // The student id comes from the login token, never from the request body.
    @PostMapping
    public ResponseEntity<?> placeOrder(
            @RequestAttribute("auth.refId") Integer authStudentId,
            @RequestBody PlaceOrderRequest request) {

        try {
            return ResponseEntity.ok(
                    orderService.placeOrder(authStudentId, request));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{orderId}/store/{storeId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Integer orderId,
            @PathVariable Integer storeId,
            @RequestBody Map<String, String> request) {

        try {
            return ResponseEntity.ok(
                    orderService.updateOrderStatus(
                            orderId, storeId, request.get("status")));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{orderId}/student/{studentId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Integer orderId,
            @PathVariable Integer studentId) {

        try {
            return ResponseEntity.ok(
                    orderService.cancelOrderByStudent(orderId, studentId));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/store/{storeId}/selected")
public ResponseEntity<?> deleteSelectedOrders(
        @PathVariable Integer storeId,
        @RequestBody List<Integer> orderIds) {
    try {
        orderService.deleteSelectedOrders(storeId, orderIds);
        return ResponseEntity.ok("Selected orders deleted successfully.");
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
@DeleteMapping("/student/{studentId}/selected")
public ResponseEntity<?> clearSelectedStudentOrders(
        @PathVariable Integer studentId,
        @RequestBody List<Integer> orderIds) {

    try {
        orderService.clearSelectedStudentOrders(studentId, orderIds);

        return ResponseEntity.ok(
                "Selected student orders cleared successfully."
        );

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

}
