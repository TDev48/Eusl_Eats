package com.eusleats.eusleats.dto;

public record AdminStoreResponse(
        Integer storeId,
        String storeName,
        String location,
        String description,
        String approvalStatus,
        String ownerName,
        String ownerEmail,
        String ownerPhone) {
}
