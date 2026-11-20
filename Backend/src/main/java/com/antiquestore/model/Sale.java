package com.antiquestore.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Sale {
    private Integer saleId;
    private Integer itemId;
    private String buyerName;
    private BigDecimal salePrice;
    private LocalDate saleDate;

    public Sale() {
    }

    public Sale(Integer saleId, Integer itemId, String buyerName, BigDecimal salePrice, LocalDate saleDate) {
        this.saleId = saleId;
        this.itemId = itemId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale = (Sale) o;
        return Objects.equals(saleId, sale.saleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saleId);
    }

    @Override
    public String toString() {
        return "Sale{" +
                "saleId=" + saleId +
                ", itemId=" + itemId +
                ", buyerName='" + buyerName + '\'' +
                ", salePrice=" + salePrice +
                ", saleDate=" + saleDate +
                '}';
    }
}
