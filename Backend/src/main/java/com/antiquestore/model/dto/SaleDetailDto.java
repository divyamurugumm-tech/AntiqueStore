package com.antiquestore.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SaleDetailDto {
    private Integer saleId;
    private Integer itemId;
    private String itemName;
    private String buyerName;
    private BigDecimal salePrice;
    private LocalDate saleDate;

    public SaleDetailDto() {
    }

    public SaleDetailDto(Integer saleId, Integer itemId, String itemName, String buyerName, BigDecimal salePrice, LocalDate saleDate) {
        this.saleId = saleId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.buyerName = buyerName;
        this.salePrice = salePrice;
        this.saleDate = saleDate;
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }
}
