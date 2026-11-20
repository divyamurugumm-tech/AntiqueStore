package com.antiquestore.controller;

import com.antiquestore.model.Item;
import com.antiquestore.model.dto.ItemRequest;
import com.antiquestore.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * GET /api/items - list all items (from DB)
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    /**
     * GET /api/items/sorted - BST in-order traversal
     */
    @GetMapping("/sorted")
    public ResponseEntity<List<Item>> getSortedItems() {
        return ResponseEntity.ok(itemService.getSortedItems());
    }

    /**
     * GET /api/items/range?min=&max= - BST range search by price
     */
    @GetMapping("/range")
    public ResponseEntity<List<Item>> getItemsByPriceRange(
            @RequestParam(required = false) BigDecimal min,
            @RequestParam(required = false) BigDecimal max) {
        return ResponseEntity.ok(itemService.getItemsByPriceRange(min, max));
    }

    /**
     * GET /api/items/{id} - search via BST
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Integer id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    /**
     * POST /api/items - creates item in MySQL first, then BST
     */
    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody ItemRequest req) {
        Item created = itemService.createItem(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/items/{id} - updates item in MySQL first, then BST
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @PathVariable Integer id,
            @Valid @RequestBody ItemRequest req) {
        Item updated = itemService.updateItem(id, req);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/items/{id} - deletes from MySQL first, then BST
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteItem(@PathVariable Integer id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok(Map.of("message", "Item deleted successfully", "itemId", String.valueOf(id)));
    }
}
