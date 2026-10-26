
package com.eusleats.eusleats.dto;

import java.math.BigDecimal;
import java.util.List;

public class FoodItemResponse {

    private Integer foodId;
    private Integer storeId;
    private Integer categoryId;
    private String foodName;
    private String description;
    private BigDecimal price;
    private String image;
    private Boolean availability;
    private List<FoodSizeResponse> sizes;

    public FoodItemResponse(
            Integer foodId,
            Integer storeId,
            Integer categoryId,
            String foodName,
            String description,
            BigDecimal price,
            String image,
            Boolean availability
    ) {
        this.foodId = foodId;
        this.storeId = storeId;
        this.categoryId = categoryId;
        this.foodName = foodName;
        this.description = description;
        this.price = price;
        this.image = image;
        this.availability = availability;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public List<FoodSizeResponse> getSizes() {
        return sizes;
    }

    public void setSizes(List<FoodSizeResponse> sizes) {
        this.sizes = sizes;
    }

    public static class FoodSizeResponse {

        private String sizeName;
        private BigDecimal price;

        public FoodSizeResponse(String sizeName, BigDecimal price) {
            this.sizeName = sizeName;
            this.price = price;
        }

        public String getSizeName() {
            return sizeName;
        }

        public BigDecimal getPrice() {
            return price;
        }
    }
}
