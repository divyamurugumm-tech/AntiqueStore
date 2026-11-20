package com.antiquestore.service;

import com.antiquestore.exception.BadRequestException;
import com.antiquestore.exception.ConflictException;
import com.antiquestore.exception.ResourceNotFoundException;
import com.antiquestore.model.Era;
import com.antiquestore.model.dto.EraRequest;
import com.antiquestore.repository.EraRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EraService {

    private final EraRepository eraRepository;

    public EraService(EraRepository eraRepository) {
        this.eraRepository = eraRepository;
    }

    public List<Era> getAllEras() {
        return eraRepository.findAll();
    }

    public Era getEraById(Integer id) {
        return eraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Era with ID " + id + " not found"));
    }

    public Era createEra(EraRequest req) {
        if (req.getStartYear() > req.getEndYear()) {
            throw new BadRequestException("Start year (" + req.getStartYear() + ") must be less than or equal to end year (" + req.getEndYear() + ")");
        }

        if (eraRepository.findByName(req.getEraName().trim()).isPresent()) {
            throw new ConflictException("Era with name '" + req.getEraName().trim() + "' already exists");
        }

        Era era = new Era();
        era.setEraName(req.getEraName().trim());
        era.setStartYear(req.getStartYear());
        era.setEndYear(req.getEndYear());

        return eraRepository.insert(era);
    }
}
