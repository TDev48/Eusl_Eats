
package com.eusleats.eusleats.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import jakarta.persistence.Column;


@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Integer orderItemId;

    @Column(name = "selected_size", length = 20)
private String selectedSize;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "food_id", nullable = false)
    private Integer foodId;

    @Column(name = "food_name", nullable = false)
    private String foodName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public String getSelectedSize() {
    return selectedSize;
}

public void setSelectedSize(String selectedSize) {
    this.selectedSize = selectedSize;
}

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getFoodName() {
    return foodName;
}

public void setFoodName(String foodName) {
    this.foodName = foodName;
}
}
