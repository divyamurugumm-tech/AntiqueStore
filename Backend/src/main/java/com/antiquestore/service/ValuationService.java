package com.antiquestore.service;

import com.antiquestore.exception.ResourceNotFoundException;
import com.antiquestore.model.Valuation;
import com.antiquestore.model.dto.ValuationRequest;
import com.antiquestore.repository.ItemRepository;
import com.antiquestore.repository.ValuationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValuationService {

    private final ValuationRepository valuationRepository;
    private final ItemRepository itemRepository;

    public ValuationService(ValuationRepository valuationRepository, ItemRepository itemRepository) {
        this.valuationRepository = valuationRepository;
        this.itemRepository = itemRepository;
    }

    public Valuation addValuation(ValuationRequest req) {
        if (!itemRepository.existsById(req.getItemId())) {
            throw new ResourceNotFoundException("Cannot add valuation: Item with ID " + req.getItemId() + " not found");
        }

        Valuation val = new Valuation();
        val.setItemId(req.getItemId());
        val.setAppraisedValue(req.getAppraisedValue());
        val.setValDate(req.getValDate());
        val.setAppraiser(req.getAppraiser() != null ? req.getAppraiser().trim() : null);

        return valuationRepository.insert(val);
    }

    public List<Valuation> getValuationsByItemId(Integer itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item with ID " + itemId + " not found");
        }
        return valuationRepository.findByItemId(itemId);
    }
}
