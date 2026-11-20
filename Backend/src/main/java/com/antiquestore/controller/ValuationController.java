package com.antiquestore.controller;

import com.antiquestore.model.Valuation;
import com.antiquestore.model.dto.ValuationRequest;
import com.antiquestore.service.ValuationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ValuationController {

    private final ValuationService valuationService;

    public ValuationController(ValuationService valuationService) {
        this.valuationService = valuationService;
    }

    /**
     * POST /api/valuations - record a new valuation
     */
    @PostMapping("/api/valuations")
    public ResponseEntity<Valuation> createValuation(@Valid @RequestBody ValuationRequest req) {
        Valuation created = valuationService.addValuation(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/items/{id}/valuations - list valuations for a specific item
     */
    @GetMapping("/api/items/{id}/valuations")
    public ResponseEntity<List<Valuation>> getValuationsByItemId(@PathVariable Integer id) {
        return ResponseEntity.ok(valuationService.getValuationsByItemId(id));
    }
}
