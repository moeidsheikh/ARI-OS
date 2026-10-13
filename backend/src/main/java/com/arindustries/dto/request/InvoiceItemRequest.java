package com.arindustries.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class InvoiceItemRequest {
    @NotBlank private String category;
    @NotBlank private String thickness;
    @NotBlank private String productType;
    @NotBlank private String size;
    private String packaging;
    @Min(1) private int pcsPerBox = 20;
    @Min(1) private int quantity;
    private String billingUnit = "Boxes";
    @NotNull @Positive private BigDecimal ratePerPc;
    private BigDecimal discountPercent = BigDecimal.ZERO;
    private BigDecimal cgstPercent = BigDecimal.ZERO;
    private BigDecimal sgstPercent = BigDecimal.ZERO;
    private BigDecimal igstPercent = BigDecimal.ZERO;

    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public String getThickness() { return thickness; }
    public void setThickness(String v) { this.thickness = v; }
    public String getProductType() { return productType; }
    public void setProductType(String v) { this.productType = v; }
    public String getSize() { return size; }
    public void setSize(String v) { this.size = v; }
    public String getPackaging() { return packaging; }
    public void setPackaging(String v) { this.packaging = v; }
    public int getPcsPerBox() { return pcsPerBox; }
    public void setPcsPerBox(int v) { this.pcsPerBox = v; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int v) { this.quantity = v; }
    public String getBillingUnit() { return billingUnit; }
    public void setBillingUnit(String v) { this.billingUnit = v; }
    public BigDecimal getRatePerPc() { return ratePerPc; }
    public void setRatePerPc(BigDecimal v) { this.ratePerPc = v; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal v) { this.discountPercent = v; }
    public BigDecimal getCgstPercent() { return cgstPercent; }
    public void setCgstPercent(BigDecimal v) { this.cgstPercent = v; }
    public BigDecimal getSgstPercent() { return sgstPercent; }
    public void setSgstPercent(BigDecimal v) { this.sgstPercent = v; }
    public BigDecimal getIgstPercent() { return igstPercent; }
    public void setIgstPercent(BigDecimal v) { this.igstPercent = v; }
}
