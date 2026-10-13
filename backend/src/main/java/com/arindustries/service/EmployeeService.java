package com.arindustries.service;

import com.arindustries.dto.request.CreateEmployeeRequest;
import com.arindustries.dto.response.EmployeeResponse;
import com.arindustries.entity.Employee;
import com.arindustries.exception.ResourceNotFoundException;
import com.arindustries.mapper.EntityDtoMapper;
import com.arindustries.pagination.PageResponse;
import com.arindustries.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EntityDtoMapper mapper;
    private final AuditLogService auditLogService;

    public EmployeeService(EmployeeRepository employeeRepository, EntityDtoMapper mapper, AuditLogService auditLogService) {
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getAll(int page, int size, String search) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName"));
        Page<Employee> pageResult = employeeRepository.findByActiveAndSearch(
            true,
            search != null && !search.isBlank() ? search.trim() : null,
            pageRequest
        );

        List<EmployeeResponse> dtos = pageResult.getContent().stream()
            .map(mapper::toEmployeeResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(dtos, pageResult.getTotalElements(), pageResult.getNumber(), pageResult.getSize());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        Employee emp = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return mapper.toEmployeeResponse(emp);
    }

    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest req, String username) {
        Employee emp = new Employee();
        emp.setFirstName(req.getFirstName());
        emp.setLastName(req.getLastName());
        emp.setEmail(req.getEmail());
        emp.setPhone(req.getPhone());
        emp.setDateOfBirth(req.getDateOfBirth());
        emp.setAddress(req.getAddress());
        emp.setJoiningDate(req.getJoiningDate());
        emp.setDepartment(req.getDepartment());
        emp.setDesignation(req.getDesignation());
        emp.setEmploymentType(req.getEmploymentType() != null ? req.getEmploymentType() : "FULL_TIME");
        emp.setWorkLocation(req.getWorkLocation() != null ? req.getWorkLocation() : "Nagpur Plant");
        emp.setShift(req.getShift() != null ? req.getShift() : "General (8:30 AM - 5:30 PM)");
        emp.setBasicSalary(req.getBasicSalary() != null ? req.getBasicSalary() : BigDecimal.ZERO);
        emp.setActive(true);

        long count = employeeRepository.count();
        long nextNum = 101 + count;
        String code = String.format("EMP-%03d", nextNum);
        while (employeeRepository.existsByEmployeeCode(code)) {
            nextNum++;
            code = String.format("EMP-%03d", nextNum);
        }
        emp.setEmployeeCode(code);

        Employee saved = employeeRepository.save(emp);
        auditLogService.log(username, "CREATE", "EMPLOYEE",
            "Created employee: " + saved.getEmployeeCode() + " " + saved.getFirstName() + " " + saved.getLastName(),
            "Employee", saved.getId().toString());

        return mapper.toEmployeeResponse(saved);
    }

    @Transactional
    public EmployeeResponse update(Long id, CreateEmployeeRequest req, String username) {
        Employee emp = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        emp.setFirstName(req.getFirstName());
        emp.setLastName(req.getLastName());
        if (req.getEmail() != null) emp.setEmail(req.getEmail());
        emp.setPhone(req.getPhone());
        if (req.getDateOfBirth() != null) emp.setDateOfBirth(req.getDateOfBirth());
        if (req.getAddress() != null) emp.setAddress(req.getAddress());
        if (req.getJoiningDate() != null) emp.setJoiningDate(req.getJoiningDate());
        emp.setDepartment(req.getDepartment());
        emp.setDesignation(req.getDesignation());
        if (req.getEmploymentType() != null) emp.setEmploymentType(req.getEmploymentType());
        if (req.getWorkLocation() != null) emp.setWorkLocation(req.getWorkLocation());
        if (req.getShift() != null) emp.setShift(req.getShift());
        if (req.getBasicSalary() != null) emp.setBasicSalary(req.getBasicSalary());

        Employee saved = employeeRepository.save(emp);
        auditLogService.log(username, "UPDATE", "EMPLOYEE",
            "Updated employee: " + saved.getEmployeeCode(), "Employee", saved.getId().toString());

        return mapper.toEmployeeResponse(saved);
    }

    @Transactional
    public void delete(Long id, String username) {
        Employee emp = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        emp.setActive(false);
        employeeRepository.save(emp);
        auditLogService.log(username, "DEACTIVATE", "EMPLOYEE",
            "Deactivated employee: " + emp.getEmployeeCode(), "Employee", emp.getId().toString());
    }
}
