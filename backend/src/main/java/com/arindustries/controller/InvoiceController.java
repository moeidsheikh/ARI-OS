package com.arindustries.controller;

import com.arindustries.dto.request.CreateInvoiceRequest;
import com.arindustries.dto.response.InvoiceResponse;
import com.arindustries.pagination.PageResponse;
import com.arindustries.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/invoices")
@Tag(name = "Invoices & Billing", description = "GST Tax Invoices and Billing APIs")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    @Operation(summary = "Get paginated invoices")
    public ResponseEntity<PageResponse<InvoiceResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus) {
        return ResponseEntity.ok(invoiceService.getAll(page, size, search, status, paymentStatus));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create and finalize new tax invoice")
    public ResponseEntity<InvoiceResponse> create(
            @Valid @RequestBody CreateInvoiceRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(request, user));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel tax invoice (restores stock & ledger)")
    public ResponseEntity<InvoiceResponse> cancel(
            @PathVariable Long id,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(invoiceService.cancel(id, user));
    }
}
