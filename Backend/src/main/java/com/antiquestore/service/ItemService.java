package com.antiquestore.service;

import com.antiquestore.ds.BinarySearchTree;
import com.antiquestore.exception.BadRequestException;
import com.antiquestore.exception.ResourceNotFoundException;
import com.antiquestore.model.Item;
import com.antiquestore.model.ItemStatus;
import com.antiquestore.model.dto.ItemRequest;
import com.antiquestore.repository.EraRepository;
import com.antiquestore.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;
    private final EraRepository eraRepository;
    private final BinarySearchTree bst;

    public ItemService(ItemRepository itemRepository, EraRepository eraRepository, BinarySearchTree bst) {
        this.itemRepository = itemRepository;
        this.eraRepository = eraRepository;
        this.bst = bst;
    }

    /**
     * At startup, load all AVAILABLE items from MySQL into the BST.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadAvailableItemsIntoBst() {
        try {
            log.info("Loading AVAILABLE items from MySQL into in-memory Binary Search Tree...");
            List<Item> availableItems = itemRepository.findByStatus(ItemStatus.AVAILABLE);
            bst.clear();
            for (Item item : availableItems) {
                bst.insert(item);
            }
            log.info("Successfully loaded {} AVAILABLE items into BST (BST size: {}).", availableItems.size(), bst.size());
        } catch (Exception e) {
            log.error("Failed to load AVAILABLE items into BST at startup: {}", e.getMessage());
        }
    }

    /**
     * GET /api/items - list all items directly from DB.
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    /**
     * GET /api/items/{id} - search via BST.
     */
    public Item getItemById(Integer id) {
        Item item = bst.search(id);
        if (item == null) {
            throw new ResourceNotFoundException("Item with ID " + id + " not found in available items");
        }
        return item;
    }

    /**
     * GET /api/items/sorted - BST in-order traversal.
     */
    public List<Item> getSortedItems() {
        return bst.inorder();
    }

    /**
     * GET /api/items/range?min=&max= - BST range search by price.
     */
    public List<Item> getItemsByPriceRange(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BadRequestException("Minimum price (" + min + ") cannot be greater than maximum price (" + max + ")");
        }
        return bst.rangeSearch(min, max);
    }

    /**
     * POST /api/items - creates item in MySQL first, then updates BST if AVAILABLE.
     */
    public Item createItem(ItemRequest req) {
        if (!eraRepository.existsById(req.getEraId())) {
            throw new BadRequestException("Cannot create item: Era with ID " + req.getEraId() + " does not exist");
        }

        Item item = new Item();
        item.setName(req.getName().trim());
        item.setCategory(req.getCategory().trim());
        item.setEraId(req.getEraId());
        item.setItemCondition(req.getCondition().trim());
        item.setPrice(req.getPrice());
        item.setStatus(req.getStatus() != null ? req.getStatus() : ItemStatus.AVAILABLE);

        // 1. Write to MySQL first
        Item saved = itemRepository.insert(item);

        // Populate era name if possible
        eraRepository.findById(saved.getEraId()).ifPresent(era -> saved.setEraName(era.getEraName()));

        // 2. Write to BST if status is AVAILABLE
        if (saved.getStatus() == ItemStatus.AVAILABLE) {
            bst.insert(saved);
        }

        return saved;
    }

    /**
     * PUT /api/items/{id} - updates item in MySQL first, then updates BST.
     */
    public Item updateItem(Integer id, ItemRequest req) {
        Item existing = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item with ID " + id + " not found"));

        if (!eraRepository.existsById(req.getEraId())) {
            throw new BadRequestException("Cannot update item: Era with ID " + req.getEraId() + " does not exist");
        }

        existing.setName(req.getName().trim());
        existing.setCategory(req.getCategory().trim());
        existing.setEraId(req.getEraId());
        existing.setItemCondition(req.getCondition().trim());
        existing.setPrice(req.getPrice());
        existing.setStatus(req.getStatus() != null ? req.getStatus() : ItemStatus.AVAILABLE);

        // 1. Write to MySQL first
        itemRepository.update(existing);

        eraRepository.findById(existing.getEraId()).ifPresent(era -> existing.setEraName(era.getEraName()));

        // 2. Synchronize to BST
        if (existing.getStatus() == ItemStatus.AVAILABLE) {
            bst.insert(existing);
        } else {
            bst.delete(id);
        }

        return existing;
    }

    /**
     * DELETE /api/items/{id} - deletes from MySQL first, then deletes from BST.
     */
    public void deleteItem(Integer id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item with ID " + id + " not found");
        }

        // 1. Delete from MySQL first
        itemRepository.deleteById(id);

        // 2. Remove from BST
        bst.delete(id);
    }
}
