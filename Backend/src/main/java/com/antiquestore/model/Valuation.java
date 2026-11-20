package com.antiquestore.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Valuation {
    private Integer valId;
    private Integer itemId;
    private BigDecimal appraisedValue;
    private LocalDate valDate;
    private String appraiser;

    public Valuation() {
    }

    public Valuation(Integer valId, Integer itemId, BigDecimal appraisedValue, LocalDate valDate, String appraiser) {
        this.valId = valId;
        this.itemId = itemId;
        this.appraisedValue = appraisedValue;
        this.valDate = valDate;
        this.appraiser = appraiser;
    }

    public Integer getValId() {
        return valId;
    }

    public void setValId(Integer valId) {
        this.valId = valId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Valuation valuation = (Valuation) o;
        return Objects.equals(valId, valuation.valId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valId);
    }

    @Override
    public String toString() {
        return "Valuation{" +
                "valId=" + valId +
                ", itemId=" + itemId +
                ", appraisedValue=" + appraisedValue +
                ", valDate=" + valDate +
                ", appraiser='" + appraiser + '\'' +
                '}';
    }
}
