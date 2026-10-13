package com.arindustries.controller;

import com.arindustries.dto.request.CreateCustomerRequest;
import com.arindustries.dto.response.CustomerResponse;
import com.arindustries.pagination.PageResponse;
import com.arindustries.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customer Management APIs")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "Get paginated list of customers")
    public ResponseEntity<PageResponse<CustomerResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(customerService.getAll(page, size, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<CustomerResponse> create(
            @Valid @RequestBody CreateCustomerRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing customer")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateCustomerRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(customerService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate customer")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        customerService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
