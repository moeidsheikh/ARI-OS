package com.arindustries.controller;

import com.arindustries.dto.request.CreateEmployeeRequest;
import com.arindustries.dto.response.EmployeeResponse;
import com.arindustries.pagination.PageResponse;
import com.arindustries.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employees", description = "Employee Directory and HR Management APIs")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "Get paginated employees list")
    public ResponseEntity<PageResponse<EmployeeResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(employeeService.getAll(page, size, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Add a new employee")
    public ResponseEntity<EmployeeResponse> create(
            @Valid @RequestBody CreateEmployeeRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee details")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateEmployeeRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(employeeService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate employee")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        employeeService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
