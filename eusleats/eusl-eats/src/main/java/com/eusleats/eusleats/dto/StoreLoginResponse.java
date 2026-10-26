package com.eusleats.eusleats.dto;

public class StoreLoginResponse {

    private Integer userId;
    private Integer storeId;
    private String fullName;
    private String email;
    private String phone;
    private String storeName;
    private String location;
    private String description;
    private String approvalStatus;
    private String token;

    public StoreLoginResponse(
            Integer userId,
            Integer storeId,
            String fullName,
            String email,
            String phone,
            String storeName,
            String location,
            String description,
            String approvalStatus,
            String token) {

        this.userId = userId;
        this.storeId = storeId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.storeName = storeName;
        this.location = location;
        this.description = description;
        this.approvalStatus = approvalStatus;
        this.token = token;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public String getToken() {
        return token;
    }
}
