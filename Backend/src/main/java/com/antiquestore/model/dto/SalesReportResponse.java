package com.antiquestore.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class SalesReportResponse {
    private List<SaleDetailDto> sales;
    private BigDecimal totalRevenue;
    private int totalCount;

    public SalesReportResponse() {
    }

    public SalesReportResponse(List<SaleDetailDto> sales, BigDecimal totalRevenue, int totalCount) {
        this.sales = sales;
        this.totalRevenue = totalRevenue;
        this.totalCount = totalCount;
    }

    public List<SaleDetailDto> getSales() {
        return sales;
    }

    public void setSales(List<SaleDetailDto> sales) {
        this.sales = sales;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
