package com.arindustries.controller;

import com.arindustries.dto.request.StockAdjustmentRequest;
import com.arindustries.dto.response.InventoryResponse;
import com.arindustries.dto.response.StockMovementResponse;
import com.arindustries.pagination.PageResponse;
import com.arindustries.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory", description = "Finished Goods Inventory and Stock Movements APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "Get paginated inventory stock list")
    public ResponseEntity<PageResponse<InventoryResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean lowStock) {
        return ResponseEntity.ok(inventoryService.getAll(page, size, category, search, lowStock));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory item by ID")
    public ResponseEntity<InventoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }

    @PostMapping("/adjust")
    @Operation(summary = "Adjust inventory stock (manual increment/decrement/set)")
    public ResponseEntity<InventoryResponse> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(inventoryService.adjustStock(request, user));
    }

    @PostMapping
    @Operation(summary = "Create a new inventory item")
    public ResponseEntity<InventoryResponse> create(
            @Valid @RequestBody com.arindustries.dto.request.CreateInventoryRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(inventoryService.create(request, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update inventory item details")
    public ResponseEntity<InventoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody com.arindustries.dto.request.CreateInventoryRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(inventoryService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate inventory item")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        inventoryService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/movements")
    @Operation(summary = "Get audit movement log for inventory item")
    public ResponseEntity<PageResponse<StockMovementResponse>> getMovements(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inventoryService.getMovements(id, page, size));
    }
}
