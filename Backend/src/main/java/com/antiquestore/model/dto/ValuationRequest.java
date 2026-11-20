package com.antiquestore.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ValuationRequest {

    @NotNull(message = "Item ID is required")
    private Integer itemId;

    @NotNull(message = "Appraised value is required")
    @DecimalMin(value = "0.00", message = "Appraised value must be non-negative")
    private BigDecimal appraisedValue;

    @NotNull(message = "Valuation date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate valDate;

    @Size(max = 80, message = "Appraiser name cannot exceed 80 characters")
    private String appraiser;

    public ValuationRequest() {
    }

    public ValuationRequest(Integer itemId, BigDecimal appraisedValue, LocalDate valDate, String appraiser) {
        this.itemId = itemId;
        this.appraisedValue = appraisedValue;
        this.valDate = valDate;
        this.appraiser = appraiser;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getAppraisedValue() {
        return appraisedValue;
    }

    public void setAppraisedValue(BigDecimal appraisedValue) {
        this.appraisedValue = appraisedValue;
    }

    public LocalDate getValDate() {
        return valDate;
    }

    public void setValDate(LocalDate valDate) {
        this.valDate = valDate;
    }

    public String getAppraiser() {
        return appraiser;
    }

    public void setAppraiser(String appraiser) {
        this.appraiser = appraiser;
    }
}
