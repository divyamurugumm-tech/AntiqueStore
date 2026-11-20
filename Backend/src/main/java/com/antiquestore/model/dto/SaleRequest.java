package com.antiquestore.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SaleRequest {

    @NotNull(message = "Item ID is required")
    private Integer itemId;

    @NotBlank(message = "Buyer name is required")
    @Size(max = 100, message = "Buyer name cannot exceed 100 characters")
    private String buyerName;

    @NotNull(message = "Sale price is required")
    @DecimalMin(value = "0.00", message = "Sale price must be non-negative")
    private BigDecimal salePrice;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate saleDate;

    public SaleRequest() {
    }

    public SaleRequest(Integer itemId, String buyerName, BigDecimal salePrice, LocalDate saleDate) {
        this.itemId = itemId;
        this.buyerName = buyerName;
        this.salePrice = salePrice;
        this.saleDate = saleDate;
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
}
