package com.eusleats.eusleats.dto;

public class FoodStoreResponse {

    private Integer storeId;
    private String storeName;
    private String location;
    private String description;
    private String approvalStatus;

    public FoodStoreResponse(
            Integer storeId,
            String storeName,
            String location,
            String description,
            String approvalStatus) {

        this.storeId = storeId;
        this.storeName = storeName;
        this.location = location;
        this.description = description;
        this.approvalStatus = approvalStatus;
    }

    public Integer getStoreId() {
        return storeId;
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
}