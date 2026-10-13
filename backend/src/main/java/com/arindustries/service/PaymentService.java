package com.arindustries.service;

import com.arindustries.dto.request.RecordPaymentRequest;
import com.arindustries.dto.response.PaymentResponse;
import com.arindustries.entity.Customer;
import com.arindustries.entity.Invoice;
import com.arindustries.entity.Payment;
import com.arindustries.enums.PaymentMode;
import com.arindustries.enums.PaymentStatus;
import com.arindustries.exception.ResourceNotFoundException;
import com.arindustries.mapper.EntityDtoMapper;
import com.arindustries.pagination.PageResponse;
import com.arindustries.repository.CustomerRepository;
import com.arindustries.repository.InvoiceRepository;
import com.arindustries.repository.PaymentRepository;
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
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final EntityDtoMapper mapper;
    private final AuditLogService auditLogService;

    public PaymentService(PaymentRepository paymentRepository,
                          CustomerRepository customerRepository,
                          InvoiceRepository invoiceRepository,
                          EntityDtoMapper mapper,
                          AuditLogService auditLogService) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getAll(int page, int size, Long customerId) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate", "id"));
        Page<Payment> pageResult;
        if (customerId != null) {
            pageResult = paymentRepository.findByCustomerId(customerId, pageRequest);
        } else {
            pageResult = paymentRepository.findAll(pageRequest);
        }

        List<PaymentResponse> dtos = pageResult.getContent().stream()
            .map(mapper::toPaymentResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(dtos, pageResult.getTotalElements(), pageResult.getNumber(), pageResult.getSize());
    }

    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id) {
        Payment pay = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return mapper.toPaymentResponse(pay);
    }

    @Transactional
    public PaymentResponse recordPayment(RecordPaymentRequest req, String username) {
        Customer customer = customerRepository.findById(req.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", req.getCustomerId()));

        Payment payment = new Payment();
        payment.setCustomer(customer);
        payment.setPaymentDate(req.getPaymentDate() != null ? req.getPaymentDate() : LocalDate.now());
        payment.setAmount(req.getAmount());

        PaymentMode mode = PaymentMode.CASH;
        if (req.getPaymentMode() != null) {
            try { mode = PaymentMode.valueOf(req.getPaymentMode().toUpperCase()); } catch (Exception ignored) {}
        }
        payment.setPaymentMode(mode);
        payment.setReferenceNumber(req.getReferenceNumber());
        payment.setNotes(req.getNotes());
        payment.setRecordedBy(username != null ? username : "admin");

        if (req.getInvoiceId() != null) {
            Invoice invoice = invoiceRepository.findById(req.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", req.getInvoiceId()));
            payment.setInvoice(invoice);

            BigDecimal paidSoFar = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal newPaid = paidSoFar.add(req.getAmount());
            invoice.setAmountPaid(newPaid);
            BigDecimal newDue = invoice.getGrandTotal().subtract(newPaid);
            if (newDue.compareTo(BigDecimal.ZERO) <= 0) {
                invoice.setBalanceDue(BigDecimal.ZERO);
                invoice.setPaymentStatus(PaymentStatus.PAID);
            } else {
                invoice.setBalanceDue(newDue);
                invoice.setPaymentStatus(PaymentStatus.PARTIAL);
            }
            invoiceRepository.save(invoice);
        }

        // Reduce Customer Outstanding Balance
        BigDecimal curOut = customer.getOutstandingBalance() != null ? customer.getOutstandingBalance() : BigDecimal.ZERO;
        customer.setOutstandingBalance(curOut.subtract(req.getAmount()).max(BigDecimal.ZERO));
        customerRepository.save(customer);

        Payment saved = paymentRepository.save(payment);
        auditLogService.log(username, "RECORD_PAYMENT", "PAYMENTS",
            "Recorded payment of Rs. " + saved.getAmount() + " from " + customer.getCustomerName(), "Payment", saved.getId().toString());

        return mapper.toPaymentResponse(saved);
    }
}
