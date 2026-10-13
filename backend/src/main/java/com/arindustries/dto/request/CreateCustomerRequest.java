package com.arindustries.dto.request;

import jakarta.validation.constraints.*;
public class CreateCustomerRequest {
    @NotBlank(message = "Customer name is required")
    @Size(max = 200) private String customerName;
    private String companyName;
    @Size(max = 20) private String phone;
    private String alternatePhone;
    @Email private String email;
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", message = "Invalid GSTIN format")
    private String gstin;
    private String pan;
    private String billingAddress;
    private String shippingAddress;
    private String city;
    private String state;
    private String stateCode;
    private String pincode;
    private java.math.BigDecimal creditLimit;
    private String paymentTerms;
    private java.math.BigDecimal openingBalance;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String v) { this.customerName = v; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String v) { this.companyName = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String v) { this.alternatePhone = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getGstin() { return gstin; }
    public void setGstin(String v) { this.gstin = v; }
    public String getPan() { return pan; }
    public void setPan(String v) { this.pan = v; }
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String v) { this.billingAddress = v; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String v) { this.shippingAddress = v; }
    public String getCity() { return city; }
    public void setCity(String v) { this.city = v; }
    public String getState() { return state; }
    public void setState(String v) { this.state = v; }
    public String getStateCode() { return stateCode; }
    public void setStateCode(String v) { this.stateCode = v; }
    public String getPincode() { return pincode; }
    public void setPincode(String v) { this.pincode = v; }
    public java.math.BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(java.math.BigDecimal v) { this.creditLimit = v; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String v) { this.paymentTerms = v; }
    public java.math.BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(java.math.BigDecimal v) { this.openingBalance = v; }
}
