package com.arindustries.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RecordPaymentRequest {
    @NotNull private Long customerId;
    private Long invoiceId;
    @NotNull private LocalDate paymentDate;
    @NotNull @Positive private BigDecimal amount;
    @NotBlank private String paymentMode;
    private String referenceNumber;
    private String notes;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { this.customerId = v; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long v) { this.invoiceId = v; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate v) { this.paymentDate = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { this.amount = v; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String v) { this.paymentMode = v; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String v) { this.referenceNumber = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
}
