package com.arindustries.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerResponse {
    private Long id;
    private String customerCode;
    private String customerName;
    private String companyName;
    private String phone;
    private String alternatePhone;
    private String email;
    private String gstin;
    private String pan;
    private String billingAddress;
    private String shippingAddress;
    private String city;
    private String state;
    private String stateCode;
    private String pincode;
    private BigDecimal creditLimit;
    private String paymentTerms;
    private BigDecimal openingBalance;
    private BigDecimal outstandingBalance;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getCustomerCode() { return customerCode; } public void setCustomerCode(String v) { this.customerCode = v; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String v) { this.customerName = v; }
    public String getCompanyName() { return companyName; } public void setCompanyName(String v) { this.companyName = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { this.phone = v; }
    public String getAlternatePhone() { return alternatePhone; } public void setAlternatePhone(String v) { this.alternatePhone = v; }
    public String getEmail() { return email; } public void setEmail(String v) { this.email = v; }
    public String getGstin() { return gstin; } public void setGstin(String v) { this.gstin = v; }
    public String getPan() { return pan; } public void setPan(String v) { this.pan = v; }
    public String getBillingAddress() { return billingAddress; } public void setBillingAddress(String v) { this.billingAddress = v; }
    public String getShippingAddress() { return shippingAddress; } public void setShippingAddress(String v) { this.shippingAddress = v; }
    public String getCity() { return city; } public void setCity(String v) { this.city = v; }
    public String getState() { return state; } public void setState(String v) { this.state = v; }
    public String getStateCode() { return stateCode; } public void setStateCode(String v) { this.stateCode = v; }
    public String getPincode() { return pincode; } public void setPincode(String v) { this.pincode = v; }
    public BigDecimal getCreditLimit() { return creditLimit; } public void setCreditLimit(BigDecimal v) { this.creditLimit = v; }
    public String getPaymentTerms() { return paymentTerms; } public void setPaymentTerms(String v) { this.paymentTerms = v; }
    public BigDecimal getOpeningBalance() { return openingBalance; } public void setOpeningBalance(BigDecimal v) { this.openingBalance = v; }
    public BigDecimal getOutstandingBalance() { return outstandingBalance; } public void setOutstandingBalance(BigDecimal v) { this.outstandingBalance = v; }
    public boolean isActive() { return active; } public void setActive(boolean v) { this.active = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}
