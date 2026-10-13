package com.arindustries.dto.response;

import java.math.BigDecimal;

public class InvoiceItemResponse {
    private Long id;
    private String category;
    private String thickness;
    private String productType;
    private String size;
    private String packaging;
    private int pcsPerBox;
    private int quantity;
    private String billingUnit;
    private BigDecimal ratePerPc;
    private BigDecimal ratePerBox;
    private BigDecimal discountPercent;
    private BigDecimal taxableAmount;
    private BigDecimal cgstPercent;
    private BigDecimal sgstPercent;
    private BigDecimal igstPercent;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalAmount;

    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getCategory() { return category; } public void setCategory(String v) { this.category = v; }
    public String getThickness() { return thickness; } public void setThickness(String v) { this.thickness = v; }
    public String getProductType() { return productType; } public void setProductType(String v) { this.productType = v; }
    public String getSize() { return size; } public void setSize(String v) { this.size = v; }
    public String getPackaging() { return packaging; } public void setPackaging(String v) { this.packaging = v; }
    public int getPcsPerBox() { return pcsPerBox; } public void setPcsPerBox(int v) { this.pcsPerBox = v; }
    public int getQuantity() { return quantity; } public void setQuantity(int v) { this.quantity = v; }
    public String getBillingUnit() { return billingUnit; } public void setBillingUnit(String v) { this.billingUnit = v; }
    public BigDecimal getRatePerPc() { return ratePerPc; } public void setRatePerPc(BigDecimal v) { this.ratePerPc = v; }
    public BigDecimal getRatePerBox() { return ratePerBox; } public void setRatePerBox(BigDecimal v) { this.ratePerBox = v; }
    public BigDecimal getDiscountPercent() { return discountPercent; } public void setDiscountPercent(BigDecimal v) { this.discountPercent = v; }
    public BigDecimal getTaxableAmount() { return taxableAmount; } public void setTaxableAmount(BigDecimal v) { this.taxableAmount = v; }
    public BigDecimal getCgstPercent() { return cgstPercent; } public void setCgstPercent(BigDecimal v) { this.cgstPercent = v; }
    public BigDecimal getSgstPercent() { return sgstPercent; } public void setSgstPercent(BigDecimal v) { this.sgstPercent = v; }
    public BigDecimal getIgstPercent() { return igstPercent; } public void setIgstPercent(BigDecimal v) { this.igstPercent = v; }
    public BigDecimal getCgstAmount() { return cgstAmount; } public void setCgstAmount(BigDecimal v) { this.cgstAmount = v; }
    public BigDecimal getSgstAmount() { return sgstAmount; } public void setSgstAmount(BigDecimal v) { this.sgstAmount = v; }
    public BigDecimal getIgstAmount() { return igstAmount; } public void setIgstAmount(BigDecimal v) { this.igstAmount = v; }
    public BigDecimal getTotalAmount() { return totalAmount; } public void setTotalAmount(BigDecimal v) { this.totalAmount = v; }
}
