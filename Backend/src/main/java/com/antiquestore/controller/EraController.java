package com.antiquestore.controller;

import com.antiquestore.model.Era;
import com.antiquestore.model.dto.EraRequest;
import com.antiquestore.service.EraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eras")
public class EraController {

    private final EraService eraService;

    public EraController(EraService eraService) {
        this.eraService = eraService;
    }

    /**
     * GET /api/eras - list all eras
     */
    @GetMapping
    public ResponseEntity<List<Era>> getAllEras() {
        return ResponseEntity.ok(eraService.getAllEras());
    }

    /**
     * POST /api/eras - create a new era
     */
    @PostMapping
    public ResponseEntity<Era> createEra(@Valid @RequestBody EraRequest req) {
        Era created = eraService.createEra(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
