package com.antiquestore.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Objects;

public class Item {
    private Integer itemId;
    private String name;
    private String category;
    private Integer eraId;

    @JsonProperty("condition")
    private String itemCondition;

    private BigDecimal price;
    private ItemStatus status;
    private String eraName;

    public Item() {
    }

    public Item(Integer itemId, String name, String category, Integer eraId, String itemCondition, BigDecimal price, ItemStatus status) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.eraId = eraId;
        this.itemCondition = itemCondition;
        this.price = price;
        this.status = status;
    }

    public Item(Integer itemId, String name, String category, Integer eraId, String itemCondition, BigDecimal price, ItemStatus status, String eraName) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.eraId = eraId;
        this.itemCondition = itemCondition;
        this.price = price;
        this.status = status;
        this.eraName = eraName;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
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

    public String getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
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

    public String getEraName() {
        return eraName;
    }

    public void setEraName(String eraName) {
        this.eraName = eraName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(itemId, item.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemId=" + itemId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", eraId=" + eraId +
                ", itemCondition='" + itemCondition + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", eraName='" + eraName + '\'' +
                '}';
    }
}
