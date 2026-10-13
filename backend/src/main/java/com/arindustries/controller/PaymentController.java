package com.arindustries.controller;

import com.arindustries.dto.request.RecordPaymentRequest;
import com.arindustries.dto.response.PaymentResponse;
import com.arindustries.pagination.PageResponse;
import com.arindustries.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments & Receivables", description = "Customer Receipts and Payment Ledger APIs")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    @Operation(summary = "Get paginated payment records")
    public ResponseEntity<PageResponse<PaymentResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long customerId) {
        return ResponseEntity.ok(paymentService.getAll(page, size, customerId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Record incoming payment from customer")
    public ResponseEntity<PaymentResponse> recordPayment(
            @Valid @RequestBody RecordPaymentRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.recordPayment(request, user));
    }
}
