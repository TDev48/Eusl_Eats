package com.eusleats.eusleats.dto;

import java.util.List;

public class PlaceOrderRequest {

    private Integer studentId;
    private Integer storeId;
    private String orderLocation;
    private List<OrderItemRequest> items;

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getOrderLocation() {
        return orderLocation;
    }

    public void setOrderLocation(String orderLocation) {
        this.orderLocation = orderLocation;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public static class OrderItemRequest {

        private String selectedSize;
        private Integer foodId;
        private Integer quantity;

        public String getSelectedSize() {
            return selectedSize;
        }

        public void setSelectedSize(String selectedSize) {
            this.selectedSize = selectedSize;
        }

        public Integer getFoodId() {
            return foodId;
        }

        public void setFoodId(Integer foodId) {
            this.foodId = foodId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}