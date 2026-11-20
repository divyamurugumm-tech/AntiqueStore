package com.antiquestore.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EraRequest {

    @NotBlank(message = "Era name is required")
    @Size(max = 50, message = "Era name cannot exceed 50 characters")
    private String eraName;

    @NotNull(message = "Start year is required")
    private Integer startYear;

    @NotNull(message = "End year is required")
    private Integer endYear;

    public EraRequest() {
    }

    public EraRequest(String eraName, Integer startYear, Integer endYear) {
        this.eraName = eraName;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    public String getEraName() {
        return eraName;
    }

    public void setEraName(String eraName) {
        this.eraName = eraName;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public void setEndYear(Integer endYear) {
        this.endYear = endYear;
    }
}
