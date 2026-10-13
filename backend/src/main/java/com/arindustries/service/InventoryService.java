package com.arindustries.service;

import com.arindustries.dto.request.CreateInventoryRequest;
import com.arindustries.dto.request.StockAdjustmentRequest;
import com.arindustries.dto.response.InventoryResponse;
import com.arindustries.dto.response.StockMovementResponse;
import com.arindustries.entity.Inventory;
import com.arindustries.entity.StockMovement;
import com.arindustries.enums.StockMovementType;
import com.arindustries.exception.BusinessRuleException;
import com.arindustries.exception.DuplicateResourceException;
import com.arindustries.exception.ResourceNotFoundException;
import com.arindustries.mapper.EntityDtoMapper;
import com.arindustries.pagination.PageResponse;
import com.arindustries.repository.InventoryRepository;
import com.arindustries.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final EntityDtoMapper mapper;
    private final AuditLogService auditLogService;

    public InventoryService(InventoryRepository inventoryRepository,
                            StockMovementRepository stockMovementRepository,
                            EntityDtoMapper mapper,
                            AuditLogService auditLogService) {
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> getAll(int page, int size, String category, String search, Boolean lowStockOnly) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "itemName"));
        Page<Inventory> pageResult = inventoryRepository.findWithFilters(
            category != null && !category.isBlank() ? category : null,
            search != null && !search.isBlank() ? search.trim() : null,
            pageRequest
        );

        List<InventoryResponse> dtos = pageResult.getContent().stream()
            .filter(item -> lowStockOnly == null || !lowStockOnly || item.isLowStock())
            .map(mapper::toInventoryResponse)
            .collect(Collectors.toList());

        long total = (lowStockOnly != null && lowStockOnly) ? dtos.size() : pageResult.getTotalElements();
        return new PageResponse<>(dtos, total, pageResult.getNumber(), pageResult.getSize());
    }

    @Transactional(readOnly = true)
    public InventoryResponse getById(Long id) {
        Inventory item = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        return mapper.toInventoryResponse(item);
    }

    @Transactional
    public InventoryResponse adjustStock(StockAdjustmentRequest req, String username) {
        Inventory item = inventoryRepository.findById(req.getInventoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", req.getInventoryId()));

        int pcsPerBox = item.getPcsPerBox() > 0 ? item.getPcsPerBox() : (req.getPcsPerBox() > 0 ? req.getPcsPerBox() : 20);
        int deltaPcs = req.getQuantity();
        if ("Boxes".equalsIgnoreCase(req.getUnit())) {
            deltaPcs = req.getQuantity() * pcsPerBox;
        }

        int before = item.getCurrentStock();
        int after;
        StockMovementType movementType;

        String type = req.getAdjustmentType() != null ? req.getAdjustmentType().toUpperCase() : "ADD";
        if ("SUBTRACT".equals(type) || "DAMAGE".equals(type) || "DISPATCH".equals(type)) {
            if (before < deltaPcs) {
                throw new BusinessRuleException("Insufficient stock. Current stock is " + before + " Pcs, requested reduction is " + deltaPcs + " Pcs");
            }
            after = before - deltaPcs;
            movementType = StockMovementType.ADJUSTMENT_OUT;
        } else if ("SET".equals(type)) {
            after = deltaPcs;
            movementType = after >= before ? StockMovementType.ADJUSTMENT_IN : StockMovementType.ADJUSTMENT_OUT;
        } else {
            after = before + deltaPcs;
            movementType = StockMovementType.ADJUSTMENT_IN;
        }

        item.setCurrentStock(after);
        inventoryRepository.save(item);

        StockMovement movement = new StockMovement();
        movement.setInventory(item);
        movement.setMovementType(movementType);
        movement.setQuantity(Math.abs(after - before));
        movement.setStockBefore(before);
        movement.setStockAfter(after);
        movement.setReferenceType("MANUAL_ADJUSTMENT");
        movement.setNotes(req.getNotes() != null ? req.getNotes() : "Manual stock adjustment");
        movement.setPerformedBy(username != null ? username : "admin");
        stockMovementRepository.save(movement);

        auditLogService.log(username, "STOCK_ADJUSTMENT", "INVENTORY",
            "Adjusted stock for " + item.getItemCode() + " from " + before + " to " + after, "Inventory", item.getId().toString());

        return mapper.toInventoryResponse(item);
    }

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> getMovements(Long inventoryId, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StockMovement> pageResult = stockMovementRepository.findByInventoryId(inventoryId, pageRequest);

        List<StockMovementResponse> dtos = pageResult.getContent().stream()
            .map(mapper::toStockMovementResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(dtos, pageResult.getTotalElements(), pageResult.getNumber(), pageResult.getSize());
    }

    @Transactional(readOnly = true)
    public long getLowStockCount() {
        return inventoryRepository.findLowStockItems().size();
    }

    @Transactional
    public InventoryResponse create(CreateInventoryRequest req, String username) {
        if (inventoryRepository.findByItemCode(req.getItemCode()).isPresent()) {
            throw new DuplicateResourceException("Inventory item with code " + req.getItemCode() + " already exists");
        }
        Inventory item = new Inventory();
        item.setItemCode(req.getItemCode());
        item.setItemName(req.getItemName());
        item.setCategory(req.getCategory());
        item.setThickness(req.getThickness());
        item.setProductType(req.getProductType());
        item.setSize(req.getSize());
        item.setPackaging(req.getPackaging());
        item.setPcsPerBox(req.getPcsPerBox() > 0 ? req.getPcsPerBox() : 20);
        item.setCurrentStock(req.getCurrentStock());
        item.setMinimumStock(req.getMinimumStock());
        item.setUnit(req.getUnit() != null ? req.getUnit() : "Pcs");
        item.setActive(true);

        Inventory saved = inventoryRepository.save(item);
        auditLogService.log(username, "CREATE_INVENTORY", "INVENTORY",
            "Created inventory item " + saved.getItemCode() + " - " + saved.getItemName(), "Inventory", saved.getId().toString());
        return mapper.toInventoryResponse(saved);
    }

    @Transactional
    public InventoryResponse update(Long id, CreateInventoryRequest req, String username) {
        Inventory item = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

        item.setItemName(req.getItemName());
        if (req.getCategory() != null) item.setCategory(req.getCategory());
        if (req.getThickness() != null) item.setThickness(req.getThickness());
        if (req.getProductType() != null) item.setProductType(req.getProductType());
        if (req.getSize() != null) item.setSize(req.getSize());
        if (req.getPackaging() != null) item.setPackaging(req.getPackaging());
        if (req.getPcsPerBox() > 0) item.setPcsPerBox(req.getPcsPerBox());
        if (req.getMinimumStock() >= 0) item.setMinimumStock(req.getMinimumStock());

        Inventory saved = inventoryRepository.save(item);
        auditLogService.log(username, "UPDATE_INVENTORY", "INVENTORY",
            "Updated inventory item " + saved.getItemCode(), "Inventory", saved.getId().toString());
        return mapper.toInventoryResponse(saved);
    }

    @Transactional
    public void delete(Long id, String username) {
        Inventory item = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        item.setActive(false);
        inventoryRepository.save(item);
        auditLogService.log(username, "DELETE_INVENTORY", "INVENTORY",
            "Deactivated inventory item " + item.getItemCode(), "Inventory", item.getId().toString());
    }
}
