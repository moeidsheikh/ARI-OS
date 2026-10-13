package com.arindustries.service;

import com.arindustries.dto.request.CreatePriceRequest;
import com.arindustries.dto.response.PriceRecordResponse;
import com.arindustries.entity.PriceRecord;
import com.arindustries.exception.DuplicateResourceException;
import com.arindustries.exception.ResourceNotFoundException;
import com.arindustries.mapper.EntityDtoMapper;
import com.arindustries.pagination.PageResponse;
import com.arindustries.repository.PriceRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PriceRecordService {

    private final PriceRecordRepository priceRecordRepository;
    private final EntityDtoMapper mapper;
    private final AuditLogService auditLogService;

    public PriceRecordService(PriceRecordRepository priceRecordRepository, EntityDtoMapper mapper, AuditLogService auditLogService) {
        this.priceRecordRepository = priceRecordRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<PriceRecordResponse> getAll(int page, int size, String category, String thickness, String productType, String search) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "priceCode"));
        Page<PriceRecord> pageResult = priceRecordRepository.findWithFilters(
            category != null && !category.isBlank() ? category : null,
            thickness != null && !thickness.isBlank() ? thickness : null,
            productType != null && !productType.isBlank() ? productType : null,
            search != null && !search.isBlank() ? search.trim() : null,
            pageRequest
        );

        List<PriceRecordResponse> dtos = pageResult.getContent().stream()
            .map(mapper::toPriceRecordResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(dtos, pageResult.getTotalElements(), pageResult.getNumber(), pageResult.getSize());
    }

    @Transactional(readOnly = true)
    public PriceRecordResponse getById(Long id) {
        PriceRecord p = priceRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PriceRecord", "id", id));
        return mapper.toPriceRecordResponse(p);
    }

    @Transactional(readOnly = true)
    public PriceRecordResponse lookup(String category, String thickness, String productType, String size) {
        return priceRecordRepository.findByThicknessAndProductTypeAndSizeAndActiveTrue(thickness, productType, size)
            .map(mapper::toPriceRecordResponse)
            .orElse(null);
    }

    @Transactional
    public PriceRecordResponse create(CreatePriceRequest req, String username) {
        // Proactively check if specification already exists
        priceRecordRepository.findByThicknessAndProductTypeAndSizeAndActiveTrue(
            req.getThickness(), req.getProductType(), req.getSize()
        ).ifPresent(existing -> {
            throw new DuplicateResourceException(String.format(
                "A price record for %s %s (%s) already exists (%s: Wholesale ₹%.2f, Retail ₹%.2f). Please edit the existing record.",
                req.getThickness(), req.getProductType(), req.getSize(),
                existing.getPriceCode(), existing.getWholesaleRate(), existing.getRetailRate()
            ));
        });

        PriceRecord p = new PriceRecord();
        p.setCategory(req.getCategory());
        p.setThickness(req.getThickness());
        p.setProductType(req.getProductType());
        p.setSize(req.getSize());
        p.setPackaging(req.getPackaging() != null ? req.getPackaging() : req.getPcsPerBox() + " Pcs");
        p.setPcsPerBox(req.getPcsPerBox() > 0 ? req.getPcsPerBox() : 20);
        p.setWholesaleRate(req.getWholesaleRate());
        p.setRetailRate(req.getRetailRate());
        p.setWholesaleBoxRate(req.getWholesaleRate().multiply(BigDecimal.valueOf(p.getPcsPerBox())));
        p.setRetailBoxRate(req.getRetailRate().multiply(BigDecimal.valueOf(p.getPcsPerBox())));
        p.setEffectiveFrom(req.getEffectiveFrom() != null ? req.getEffectiveFrom() : LocalDate.now());
        p.setActive(true);

        long count = priceRecordRepository.count();
        String code = String.format("PL-%03d", count + 1);
        int attempt = 1;
        while (priceRecordRepository.findByPriceCode(code).isPresent()) {
            code = String.format("PL-%03d", count + 1 + attempt++);
        }
        p.setPriceCode(code);

        PriceRecord saved = priceRecordRepository.save(p);
        auditLogService.log(username, "CREATE", "PRICE_MASTER", "Added price record: " + saved.getPriceCode(), "PriceRecord", saved.getId().toString());
        return mapper.toPriceRecordResponse(saved);
    }

    @Transactional
    public PriceRecordResponse update(Long id, CreatePriceRequest req, String username) {
        PriceRecord p = priceRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PriceRecord", "id", id));

        // Check if updating specifications conflicts with another record
        priceRecordRepository.findByThicknessAndProductTypeAndSizeAndActiveTrue(
            req.getThickness(), req.getProductType(), req.getSize()
        ).filter(other -> !other.getId().equals(id))
         .ifPresent(existing -> {
             throw new DuplicateResourceException(String.format(
                 "Another price record (%s) already exists with specification %s %s (%s).",
                 existing.getPriceCode(), req.getThickness(), req.getProductType(), req.getSize()
             ));
         });

        p.setCategory(req.getCategory());
        p.setThickness(req.getThickness());
        p.setProductType(req.getProductType());
        p.setSize(req.getSize());
        if (req.getPackaging() != null) p.setPackaging(req.getPackaging());
        if (req.getPcsPerBox() > 0) p.setPcsPerBox(req.getPcsPerBox());
        p.setWholesaleRate(req.getWholesaleRate());
        p.setRetailRate(req.getRetailRate());
        p.setWholesaleBoxRate(req.getWholesaleRate().multiply(BigDecimal.valueOf(p.getPcsPerBox())));
        p.setRetailBoxRate(req.getRetailRate().multiply(BigDecimal.valueOf(p.getPcsPerBox())));
        if (req.getEffectiveFrom() != null) p.setEffectiveFrom(req.getEffectiveFrom());

        PriceRecord saved = priceRecordRepository.save(p);
        auditLogService.log(username, "UPDATE", "PRICE_MASTER", "Updated price record: " + saved.getPriceCode(), "PriceRecord", saved.getId().toString());
        return mapper.toPriceRecordResponse(saved);
    }

    @Transactional
    public void delete(Long id, String username) {
        PriceRecord p = priceRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PriceRecord", "id", id));
        p.setActive(false);
        priceRecordRepository.save(p);
        auditLogService.log(username, "DEACTIVATE", "PRICE_MASTER", "Deactivated price record: " + p.getPriceCode(), "PriceRecord", p.getId().toString());
    }
}
