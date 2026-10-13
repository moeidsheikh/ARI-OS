package com.arindustries.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateInvoiceRequest {
    private Long customerId;
    private String customerCode;
    private String customerName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String notes;
    private String termsAndConditions;
    @NotEmpty @Valid
    private List<InvoiceItemRequest> items;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { this.customerId = v; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String v) { this.customerCode = v; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String v) { this.customerName = v; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate v) { this.invoiceDate = v; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate v) { this.dueDate = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String v) { this.termsAndConditions = v; }
    public List<InvoiceItemRequest> getItems() { return items; }
    public void setItems(List<InvoiceItemRequest> v) { this.items = v; }
}
