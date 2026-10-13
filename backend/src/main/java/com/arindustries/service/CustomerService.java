package com.arindustries.service;

import com.arindustries.dto.request.CreateCustomerRequest;
import com.arindustries.dto.response.CustomerResponse;
import com.arindustries.entity.Customer;
import com.arindustries.exception.DuplicateResourceException;
import com.arindustries.exception.ResourceNotFoundException;
import com.arindustries.mapper.EntityDtoMapper;
import com.arindustries.pagination.PageResponse;
import com.arindustries.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final EntityDtoMapper mapper;
    private final AuditLogService auditLogService;

    public CustomerService(CustomerRepository customerRepository, EntityDtoMapper mapper, AuditLogService auditLogService) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getAll(int page, int size, String search) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "customerName"));
        Page<Customer> pageResult;
        if (search != null && !search.isBlank()) {
            pageResult = customerRepository.findByActiveAndSearch(true, search.trim(), pageRequest);
        } else {
            pageResult = customerRepository.findByActive(true, pageRequest);
        }

        List<CustomerResponse> dtos = pageResult.getContent().stream()
            .map(mapper::toCustomerResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(dtos, pageResult.getTotalElements(), pageResult.getNumber(), pageResult.getSize());
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        Customer c = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return mapper.toCustomerResponse(c);
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest req, String username) {
        if (req.getGstin() != null && !req.getGstin().isBlank() && customerRepository.existsByGstin(req.getGstin())) {
            throw new DuplicateResourceException("Customer with GSTIN " + req.getGstin() + " already exists");
        }

        Customer c = new Customer();
        c.setCustomerName(req.getCustomerName());
        c.setCompanyName(req.getCompanyName() != null ? req.getCompanyName() : req.getCustomerName());
        c.setPhone(req.getPhone());
        c.setAlternatePhone(req.getAlternatePhone());
        c.setEmail(req.getEmail());
        c.setGstin(req.getGstin());
        c.setPan(req.getPan());
        c.setBillingAddress(req.getBillingAddress());
        c.setShippingAddress(req.getShippingAddress() != null ? req.getShippingAddress() : req.getBillingAddress());
        c.setCity(req.getCity());
        c.setState(req.getState() != null ? req.getState() : "Maharashtra");
        c.setStateCode(req.getStateCode() != null ? req.getStateCode() : "27");
        c.setPincode(req.getPincode());
        c.setCreditLimit(req.getCreditLimit() != null ? req.getCreditLimit() : BigDecimal.ZERO);
        c.setPaymentTerms(req.getPaymentTerms());
        c.setOpeningBalance(req.getOpeningBalance() != null ? req.getOpeningBalance() : BigDecimal.ZERO);
        c.setOutstandingBalance(c.getOpeningBalance());
        c.setActive(true);

        long count = customerRepository.count();
        long nextNum = count + 1;
        String custCode = String.format("CUST-%02d", nextNum);
        while (customerRepository.existsByCustomerCode(custCode)) {
            nextNum++;
            custCode = String.format("CUST-%02d", nextNum);
        }
        c.setCustomerCode(custCode);

        Customer saved = customerRepository.save(c);
        auditLogService.log(username, "CREATE", "CUSTOMER", "Created customer: " + saved.getCustomerName(), "Customer", saved.getId().toString());
        return mapper.toCustomerResponse(saved);
    }

    @Transactional
    public CustomerResponse update(Long id, CreateCustomerRequest req, String username) {
        Customer c = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (req.getGstin() != null && !req.getGstin().isBlank() && 
            (c.getGstin() == null || !req.getGstin().equalsIgnoreCase(c.getGstin())) && 
            customerRepository.existsByGstin(req.getGstin())) {
            throw new DuplicateResourceException("Customer with GSTIN " + req.getGstin() + " already exists");
        }

        c.setCustomerName(req.getCustomerName());
        if (req.getCompanyName() != null) c.setCompanyName(req.getCompanyName());
        c.setPhone(req.getPhone());
        c.setAlternatePhone(req.getAlternatePhone());
        c.setEmail(req.getEmail());
        c.setGstin(req.getGstin());
        c.setPan(req.getPan());
        c.setBillingAddress(req.getBillingAddress());
        c.setShippingAddress(req.getShippingAddress());
        c.setCity(req.getCity());
        c.setState(req.getState());
        c.setStateCode(req.getStateCode());
        c.setPincode(req.getPincode());
        if (req.getCreditLimit() != null) c.setCreditLimit(req.getCreditLimit());
        if (req.getPaymentTerms() != null) c.setPaymentTerms(req.getPaymentTerms());

        Customer saved = customerRepository.save(c);
        auditLogService.log(username, "UPDATE", "CUSTOMER", "Updated customer: " + saved.getCustomerName(), "Customer", saved.getId().toString());
        return mapper.toCustomerResponse(saved);
    }

    @Transactional
    public void delete(Long id, String username) {
        Customer c = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        c.setActive(false);
        customerRepository.save(c);
        auditLogService.log(username, "DEACTIVATE", "CUSTOMER", "Deactivated customer: " + c.getCustomerName(), "Customer", c.getId().toString());
    }
}
