
package com.eusleats.eusleats.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "display_order_number")
    private Integer displayOrderNumber;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "store_id")
    private Integer storeId;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "status")
    private String status;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    // Student's requested pickup location
    @Column(name = "order_location", length = 255)
    private String orderLocation;

    @Column(name = "cleared_by_store", nullable = false)
    private boolean clearedByStore = false;

    @Column(name = "cleared_by_student", nullable = false)
    private boolean clearedByStudent = false;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderLocation() {
        return orderLocation;
    }

    public void setOrderLocation(String orderLocation) {
        this.orderLocation = orderLocation;
    }

    public boolean isClearedByStore() {
    return clearedByStore;
}

public void setClearedByStore(boolean clearedByStore) {
    this.clearedByStore = clearedByStore;
}

public boolean isClearedByStudent() {
    return clearedByStudent;
}

public void setClearedByStudent(boolean clearedByStudent) {
    this.clearedByStudent = clearedByStudent;
}

public Integer getDisplayOrderNumber() {
    return displayOrderNumber;
}

public void setDisplayOrderNumber(Integer displayOrderNumber) {
    this.displayOrderNumber = displayOrderNumber;
}
}
