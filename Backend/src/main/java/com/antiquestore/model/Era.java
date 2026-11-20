package com.antiquestore.model;

import java.util.Objects;

public class Era {
    private Integer eraId;
    private String eraName;
    private Integer startYear;
    private Integer endYear;

    public Era() {
    }

    public Era(Integer eraId, String eraName, Integer startYear, Integer endYear) {
        this.eraId = eraId;
        this.eraName = eraName;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    public Integer getEraId() {
        return eraId;
    }

    public void setEraId(Integer eraId) {
        this.eraId = eraId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Era era = (Era) o;
        return Objects.equals(eraId, era.eraId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eraId);
    }

    @Override
    public String toString() {
        return "Era{" +
                "eraId=" + eraId +
                ", eraName='" + eraName + '\'' +
                ", startYear=" + startYear +
                ", endYear=" + endYear +
                '}';
    }
}
