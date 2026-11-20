package com.antiquestore.service;

import com.antiquestore.ds.BinarySearchTree;
import com.antiquestore.exception.ConflictException;
import com.antiquestore.exception.ResourceNotFoundException;
import com.antiquestore.model.Item;
import com.antiquestore.model.ItemStatus;
import com.antiquestore.model.Sale;
import com.antiquestore.model.dto.SaleDetailDto;
import com.antiquestore.model.dto.SaleRequest;
import com.antiquestore.model.dto.SalesReportResponse;
import com.antiquestore.repository.ItemRepository;
import com.antiquestore.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ItemRepository itemRepository;
    private final BinarySearchTree bst;

    public SaleService(SaleRepository saleRepository, ItemRepository itemRepository, BinarySearchTree bst) {
        this.saleRepository = saleRepository;
        this.itemRepository = itemRepository;
        this.bst = bst;
    }

    /**
     * POST /api/sales - inserts sale in MySQL, sets item status SOLD in MySQL, and removes item from BST.
     */
    @Transactional
    public Sale recordSale(SaleRequest req) {
        Item item = itemRepository.findById(req.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot record sale: Item with ID " + req.getItemId() + " not found"));

        if (item.getStatus() == ItemStatus.SOLD || saleRepository.existsByItemId(req.getItemId())) {
            throw new ConflictException("Item with ID " + req.getItemId() + " is already marked as SOLD or already has a sale record");
        }

        Sale sale = new Sale();
        sale.setItemId(req.getItemId());
        sale.setBuyerName(req.getBuyerName().trim());
        sale.setSalePrice(req.getSalePrice());
        sale.setSaleDate(req.getSaleDate() != null ? req.getSaleDate() : LocalDate.now());

        // 1. Insert sale into MySQL
        Sale savedSale = saleRepository.insert(sale);

        // 2. Update item status to SOLD in MySQL
        itemRepository.updateStatus(req.getItemId(), ItemStatus.SOLD);

        // 3. Remove item from BST
        bst.delete(req.getItemId());

        return savedSale;
    }

    /**
     * GET /api/sales - sales report with item name and total revenue.
     */
    public SalesReportResponse getSalesReport() {
        List<SaleDetailDto> sales = saleRepository.findAllSalesWithItemName();
        BigDecimal totalRevenue = saleRepository.calculateTotalRevenue();
        int totalCount = saleRepository.countSales();

        return new SalesReportResponse(sales, totalRevenue, totalCount);
    }
}
