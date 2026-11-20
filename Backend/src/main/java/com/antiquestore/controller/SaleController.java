package com.antiquestore.controller;

import com.antiquestore.model.Sale;
import com.antiquestore.model.dto.SaleRequest;
import com.antiquestore.model.dto.SalesReportResponse;
import com.antiquestore.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    /**
     * POST /api/sales - records sale, marks item as SOLD in MySQL, and removes from BST
     */
    @PostMapping
    public ResponseEntity<Sale> createSale(@Valid @RequestBody SaleRequest req) {
        Sale created = saleService.recordSale(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/sales - sales report with item name and total revenue
     */
    @GetMapping
    public ResponseEntity<SalesReportResponse> getSalesReport() {
        return ResponseEntity.ok(saleService.getSalesReport());
    }
}
