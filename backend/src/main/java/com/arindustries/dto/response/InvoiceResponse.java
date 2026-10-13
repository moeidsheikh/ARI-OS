package com.arindustries.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private Long customerId;
    private String customerName;
    private String customerGstin;
    private String billingAddress;
    private String shippingAddress;
    private String placeOfSupply;
    private boolean isInterstate;
    private List<InvoiceItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal roundOff;
    private BigDecimal grandTotal;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private String paymentStatus;
    private String invoiceStatus;
    private String paymentMode;
    private String notes;
    private String termsAndConditions;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getInvoiceNumber() { return invoiceNumber; } public void setInvoiceNumber(String v) { this.invoiceNumber = v; }
    public LocalDate getInvoiceDate() { return invoiceDate; } public void setInvoiceDate(LocalDate v) { this.invoiceDate = v; }
    public LocalDate getDueDate() { return dueDate; } public void setDueDate(LocalDate v) { this.dueDate = v; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String v) { this.customerName = v; }
    public String getCustomerGstin() { return customerGstin; } public void setCustomerGstin(String v) { this.customerGstin = v; }
    public String getBillingAddress() { return billingAddress; } public void setBillingAddress(String v) { this.billingAddress = v; }
    public String getShippingAddress() { return shippingAddress; } public void setShippingAddress(String v) { this.shippingAddress = v; }
    public String getPlaceOfSupply() { return placeOfSupply; } public void setPlaceOfSupply(String v) { this.placeOfSupply = v; }
    public boolean isInterstate() { return isInterstate; } public void setInterstate(boolean v) { this.isInterstate = v; }
    public List<InvoiceItemResponse> getItems() { return items; } public void setItems(List<InvoiceItemResponse> v) { this.items = v; }
    public BigDecimal getSubtotal() { return subtotal; } public void setSubtotal(BigDecimal v) { this.subtotal = v; }
    public BigDecimal getCgst() { return cgst; } public void setCgst(BigDecimal v) { this.cgst = v; }
    public BigDecimal getSgst() { return sgst; } public void setSgst(BigDecimal v) { this.sgst = v; }
    public BigDecimal getIgst() { return igst; } public void setIgst(BigDecimal v) { this.igst = v; }
    public BigDecimal getRoundOff() { return roundOff; } public void setRoundOff(BigDecimal v) { this.roundOff = v; }
    public BigDecimal getGrandTotal() { return grandTotal; } public void setGrandTotal(BigDecimal v) { this.grandTotal = v; }
    public BigDecimal getAmountPaid() { return amountPaid; } public void setAmountPaid(BigDecimal v) { this.amountPaid = v; }
    public BigDecimal getBalanceDue() { return balanceDue; } public void setBalanceDue(BigDecimal v) { this.balanceDue = v; }
    public String getPaymentStatus() { return paymentStatus; } public void setPaymentStatus(String v) { this.paymentStatus = v; }
    public String getInvoiceStatus() { return invoiceStatus; } public void setInvoiceStatus(String v) { this.invoiceStatus = v; }
    public String getPaymentMode() { return paymentMode; } public void setPaymentMode(String v) { this.paymentMode = v; }
    public String getNotes() { return notes; } public void setNotes(String v) { this.notes = v; }
    public String getTermsAndConditions() { return termsAndConditions; } public void setTermsAndConditions(String v) { this.termsAndConditions = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}
