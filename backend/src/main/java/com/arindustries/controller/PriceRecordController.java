package com.arindustries.controller;

import com.arindustries.dto.request.CreatePriceRequest;
import com.arindustries.dto.response.PriceRecordResponse;
import com.arindustries.pagination.PageResponse;
import com.arindustries.service.PriceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/prices")
@Tag(name = "Price Master", description = "Product Price Master and Variant Rates APIs")
public class PriceRecordController {

    private final PriceRecordService priceRecordService;

    public PriceRecordController(PriceRecordService priceRecordService) {
        this.priceRecordService = priceRecordService;
    }

    @GetMapping
    @Operation(summary = "Get paginated price list with optional filters")
    public ResponseEntity<PageResponse<PriceRecordResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String thickness,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(priceRecordService.getAll(page, size, category, thickness, productType, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get price record by ID")
    public ResponseEntity<PriceRecordResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(priceRecordService.getById(id));
    }

    @GetMapping("/lookup")
    @Operation(summary = "Lookup price for specific variant")
    public ResponseEntity<PriceRecordResponse> lookup(
            @RequestParam(required = false) String category,
            @RequestParam String thickness,
            @RequestParam String productType,
            @RequestParam String size) {
        PriceRecordResponse res = priceRecordService.lookup(category, thickness, productType, size);
        if (res == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping
    @Operation(summary = "Create new price record")
    public ResponseEntity<PriceRecordResponse> create(
            @Valid @RequestBody CreatePriceRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.status(HttpStatus.CREATED).body(priceRecordService.create(request, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update price record")
    public ResponseEntity<PriceRecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreatePriceRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(priceRecordService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate price record")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        priceRecordService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
