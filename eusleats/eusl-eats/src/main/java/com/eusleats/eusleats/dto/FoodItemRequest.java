
package com.eusleats.eusleats.dto;

import java.math.BigDecimal;
import java.util.List;

public class FoodItemRequest {

    private Integer storeId;
    private Integer categoryId;
    private String foodName;
    private String description;
    private BigDecimal price;
    private String image;
    private Boolean availability;
    private List<FoodSizeRequest> sizes;

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    public List<FoodSizeRequest> getSizes() {
        return sizes;
    }

    public void setSizes(List<FoodSizeRequest> sizes) {
        this.sizes = sizes;
    }

    public static class FoodSizeRequest {

        private String sizeName;
        private BigDecimal price;

        public String getSizeName() {
            return sizeName;
        }

        public void setSizeName(String sizeName) {
            this.sizeName = sizeName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
