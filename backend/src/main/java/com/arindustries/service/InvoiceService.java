package com.arindustries.service;

import com.arindustries.dto.request.CreateInvoiceRequest;
import com.arindustries.dto.request.InvoiceItemRequest;
import com.arindustries.dto.response.InvoiceResponse;
import com.arindustries.entity.*;
import com.arindustries.enums.InvoiceStatus;
import com.arindustries.enums.PaymentStatus;
import com.arindustries.enums.StockMovementType;
import com.arindustries.exception.BusinessRuleException;
import com.arindustries.exception.ResourceNotFoundException;
import com.arindustries.mapper.EntityDtoMapper;
import com.arindustries.pagination.PageResponse;
import com.arindustries.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceSequenceRepository sequenceRepository;
    private final CustomerRepository customerRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final EntityDtoMapper mapper;
    private final AuditLogService auditLogService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceSequenceRepository sequenceRepository,
                          CustomerRepository customerRepository,
                          InventoryRepository inventoryRepository,
                          StockMovementRepository stockMovementRepository,
                          EntityDtoMapper mapper,
                          AuditLogService auditLogService) {
        this.invoiceRepository = invoiceRepository;
        this.sequenceRepository = sequenceRepository;
        this.customerRepository = customerRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getAll(int page, int size, String search, String status, String paymentStatus) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        InvoiceStatus invStatus = null;
        if (status != null && !status.isBlank()) {
            try { invStatus = InvoiceStatus.valueOf(status.toUpperCase()); } catch (Exception ignored) {}
        }

        PaymentStatus payStatus = null;
        if (paymentStatus != null && !paymentStatus.isBlank()) {
            try { payStatus = PaymentStatus.valueOf(paymentStatus.toUpperCase()); } catch (Exception ignored) {}
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "invoiceDate", "id"));
        Page<Invoice> pageResult = invoiceRepository.findWithFilters(
            search != null && !search.isBlank() ? search.trim() : null,
            invStatus,
            payStatus,
            null,
            null,
            pageRequest
        );

        List<InvoiceResponse> dtos = pageResult.getContent().stream()
            .map(mapper::toInvoiceResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(dtos, pageResult.getTotalElements(), pageResult.getNumber(), pageResult.getSize());
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long id) {
        Invoice inv = invoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return mapper.toInvoiceResponse(inv);
    }

    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest req, String username) {
        Customer customer = null;
        if (req.getCustomerId() != null) {
            customer = customerRepository.findById(req.getCustomerId()).orElse(null);
        }
        if (customer == null && req.getCustomerCode() != null && !req.getCustomerCode().isBlank()) {
            customer = customerRepository.findByCustomerCode(req.getCustomerCode().trim()).orElse(null);
        }
        if (customer == null && req.getCustomerName() != null && !req.getCustomerName().isBlank()) {
            customer = customerRepository.findByCustomerName(req.getCustomerName().trim()).orElse(null);
        }
        if (customer == null) {
            customer = customerRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new BusinessRuleException("No customer found. Please select or register a customer first."));
        }

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BusinessRuleException("Invoice must have at least one line item");
        }

        // Concurrency-safe sequence generation
        InvoiceSequence seq = sequenceRepository.findAndLock();
        if (seq == null) {
            seq = new InvoiceSequence();
            seq.setId(1L);
            seq.setLastValue(1000L);
        }
        long nextVal = seq.getLastValue() + 1;
        seq.setLastValue(nextVal);
        sequenceRepository.save(seq);
        String invoiceNumber = "ARI-INV-" + nextVal;

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(req.getInvoiceDate() != null ? req.getInvoiceDate() : LocalDate.now());
        invoice.setDueDate(req.getDueDate() != null ? req.getDueDate() : invoice.getInvoiceDate().plusDays(30));
        invoice.setBillingAddress(customer.getBillingAddress());
        invoice.setShippingAddress(customer.getShippingAddress() != null ? customer.getShippingAddress() : customer.getBillingAddress());
        invoice.setCustomerGstin(customer.getGstin());
        invoice.setPlaceOfSupply(customer.getState() != null ? customer.getState() : "Maharashtra");

        // Determine inter-state
        boolean isInterstate = customer.getStateCode() != null && !"27".equals(customer.getStateCode().trim());
        invoice.setInterstate(isInterstate);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal cgstTotal = BigDecimal.ZERO;
        BigDecimal sgstTotal = BigDecimal.ZERO;
        BigDecimal igstTotal = BigDecimal.ZERO;

        List<InvoiceItem> items = new ArrayList<>();

        for (InvoiceItemRequest itemReq : req.getItems()) {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setCategory(itemReq.getCategory());
            item.setThickness(itemReq.getThickness());
            item.setProductType(itemReq.getProductType());
            item.setSize(itemReq.getSize());
            item.setPackaging(itemReq.getPackaging());
            int pcsPerBox = itemReq.getPcsPerBox() > 0 ? itemReq.getPcsPerBox() : 20;
            item.setPcsPerBox(pcsPerBox);
            item.setQuantity(itemReq.getQuantity());
            item.setBillingUnit(itemReq.getBillingUnit() != null ? itemReq.getBillingUnit() : "Boxes");

            BigDecimal ratePerPc = itemReq.getRatePerPc();
            BigDecimal ratePerBox = ratePerPc.multiply(BigDecimal.valueOf(pcsPerBox));
            item.setRatePerPc(ratePerPc);
            item.setRatePerBox(ratePerBox);

            BigDecimal lineGross = ratePerBox.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            BigDecimal discountPercent = itemReq.getDiscountPercent() != null ? itemReq.getDiscountPercent() : BigDecimal.ZERO;
            BigDecimal discountAmount = lineGross.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal taxableAmount = lineGross.subtract(discountAmount);

            item.setDiscountPercent(discountPercent);
            item.setTaxableAmount(taxableAmount);

            if (isInterstate) {
                BigDecimal igstRate = new BigDecimal("18.00");
                BigDecimal igstAmt = taxableAmount.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
                item.setIgstPercent(igstRate);
                item.setIgstAmount(igstAmt);
                item.setCgstPercent(BigDecimal.ZERO);
                item.setCgstAmount(BigDecimal.ZERO);
                item.setSgstPercent(BigDecimal.ZERO);
                item.setSgstAmount(BigDecimal.ZERO);
                item.setTotalAmount(taxableAmount.add(igstAmt));
                igstTotal = igstTotal.add(igstAmt);
            } else {
                BigDecimal cgstRate = new BigDecimal("9.00");
                BigDecimal sgstRate = new BigDecimal("9.00");
                BigDecimal cgstAmt = taxableAmount.multiply(new BigDecimal("0.09")).setScale(2, RoundingMode.HALF_UP);
                BigDecimal sgstAmt = taxableAmount.multiply(new BigDecimal("0.09")).setScale(2, RoundingMode.HALF_UP);
                item.setCgstPercent(cgstRate);
                item.setCgstAmount(cgstAmt);
                item.setSgstPercent(sgstRate);
                item.setSgstAmount(sgstAmt);
                item.setIgstPercent(BigDecimal.ZERO);
                item.setIgstAmount(BigDecimal.ZERO);
                item.setTotalAmount(taxableAmount.add(cgstAmt).add(sgstAmt));
                cgstTotal = cgstTotal.add(cgstAmt);
                sgstTotal = sgstTotal.add(sgstAmt);
            }

            subtotal = subtotal.add(taxableAmount);
            items.add(item);

            // Stock Deduction
            int totalPcsToDeduct = itemReq.getQuantity() * pcsPerBox;
            Optional<Inventory> invOpt = inventoryRepository.findByThicknessAndProductTypeAndSizeAndActiveTrue(
                itemReq.getThickness(), itemReq.getProductType(), itemReq.getSize());
            if (invOpt.isPresent()) {
                Inventory invItem = invOpt.get();
                int before = invItem.getCurrentStock();
                int after = before - totalPcsToDeduct;
                invItem.setCurrentStock(after);
                inventoryRepository.save(invItem);

                StockMovement movement = new StockMovement();
                movement.setInventory(invItem);
                movement.setMovementType(StockMovementType.SALE_OUT);
                movement.setQuantity(totalPcsToDeduct);
                movement.setStockBefore(before);
                movement.setStockAfter(after);
                movement.setReferenceType("INVOICE");
                movement.setReferenceId(invoiceNumber);
                movement.setNotes("Deducted for invoice " + invoiceNumber);
                movement.setPerformedBy(username != null ? username : "admin");
                stockMovementRepository.save(movement);
            }
        }

        invoice.setItems(items);
        invoice.setSubtotal(subtotal);
        invoice.setCgst(cgstTotal);
        invoice.setSgst(sgstTotal);
        invoice.setIgst(igstTotal);

        BigDecimal rawTotal = subtotal.add(cgstTotal).add(sgstTotal).add(igstTotal);
        BigDecimal grandTotal = rawTotal.setScale(0, RoundingMode.HALF_UP);
        BigDecimal roundOff = grandTotal.subtract(rawTotal);

        invoice.setRoundOff(roundOff);
        invoice.setGrandTotal(grandTotal);
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceDue(grandTotal);
        invoice.setPaymentStatus(PaymentStatus.PENDING);
        invoice.setInvoiceStatus(InvoiceStatus.FINALIZED);
        invoice.setNotes(req.getNotes());
        invoice.setTermsAndConditions(req.getTermsAndConditions() != null ? req.getTermsAndConditions() :
            "1. Goods once sold will not be taken back. 2. Interest @ 18% p.a. will be charged after due date. 3. Subject to Nagpur jurisdiction.");
        invoice.setCreatedBy(username != null ? username : "admin");

        // Update Customer Outstanding Balance
        BigDecimal prevBal = customer.getOutstandingBalance() != null ? customer.getOutstandingBalance() : BigDecimal.ZERO;
        customer.setOutstandingBalance(prevBal.add(grandTotal));
        customerRepository.save(customer);

        Invoice saved = invoiceRepository.save(invoice);
        auditLogService.log(username, "CREATE_INVOICE", "BILLING",
            "Created and finalized invoice " + saved.getInvoiceNumber() + " total: Rs. " + saved.getGrandTotal(),
            "Invoice", saved.getId().toString());

        return mapper.toInvoiceResponse(saved);
    }

    @Transactional
    public InvoiceResponse cancel(Long id, String username) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        if (invoice.getInvoiceStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessRuleException("Invoice is already cancelled");
        }

        // Restore Inventory Stock
        for (InvoiceItem item : invoice.getItems()) {
            int pcsToRestore = item.getQuantity() * item.getPcsPerBox();
            Optional<Inventory> invOpt = inventoryRepository.findByThicknessAndProductTypeAndSizeAndActiveTrue(
                item.getThickness(), item.getProductType(), item.getSize());
            if (invOpt.isPresent()) {
                Inventory invItem = invOpt.get();
                int before = invItem.getCurrentStock();
                int after = before + pcsToRestore;
                invItem.setCurrentStock(after);
                inventoryRepository.save(invItem);

                StockMovement movement = new StockMovement();
                movement.setInventory(invItem);
                movement.setMovementType(StockMovementType.RETURN_IN);
                movement.setQuantity(pcsToRestore);
                movement.setStockBefore(before);
                movement.setStockAfter(after);
                movement.setReferenceType("INVOICE_CANCEL");
                movement.setReferenceId(invoice.getInvoiceNumber());
                movement.setNotes("Restored stock on invoice cancellation " + invoice.getInvoiceNumber());
                movement.setPerformedBy(username != null ? username : "admin");
                stockMovementRepository.save(movement);
            }
        }

        // Reduce Customer Outstanding
        Customer customer = invoice.getCustomer();
        if (customer != null) {
            BigDecimal currentOut = customer.getOutstandingBalance() != null ? customer.getOutstandingBalance() : BigDecimal.ZERO;
            customer.setOutstandingBalance(currentOut.subtract(invoice.getBalanceDue()).max(BigDecimal.ZERO));
            customerRepository.save(customer);
        }

        invoice.setInvoiceStatus(InvoiceStatus.CANCELLED);
        Invoice saved = invoiceRepository.save(invoice);

        auditLogService.log(username, "CANCEL_INVOICE", "BILLING",
            "Cancelled invoice " + saved.getInvoiceNumber(), "Invoice", saved.getId().toString());

        return mapper.toInvoiceResponse(saved);
    }
}
