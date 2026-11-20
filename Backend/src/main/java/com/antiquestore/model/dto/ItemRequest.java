package com.antiquestore.model.dto;

import com.antiquestore.model.ItemStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 100, message = "Item name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    @NotNull(message = "Era ID is required")
    private Integer eraId;

    @NotBlank(message = "Condition is required")
    @Size(max = 30, message = "Condition cannot exceed 30 characters")
    @JsonProperty("condition")
    @JsonAlias({"itemCondition", "condition"})
    private String condition;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    private BigDecimal price;

    private ItemStatus status = ItemStatus.AVAILABLE;

    public ItemRequest() {
    }

    public ItemRequest(String name, String category, Integer eraId, String condition, BigDecimal price, ItemStatus status) {
        this.name = name;
        this.category = category;
        this.eraId = eraId;
        this.condition = condition;
        this.price = price;
        if (status != null) {
            this.status = status;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getEraId() {
        return eraId;
    }

    public void setEraId(Integer eraId) {
        this.eraId = eraId;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }
}
