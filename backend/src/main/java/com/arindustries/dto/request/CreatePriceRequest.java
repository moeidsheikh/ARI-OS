package com.arindustries.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class CreatePriceRequest {
    @NotBlank private String category;
    @NotBlank private String thickness;
    @NotBlank private String productType;
    @NotBlank private String size;
    private String packaging;
    @Min(1) private int pcsPerBox = 20;
    @NotNull @Positive private BigDecimal wholesaleRate;
    @NotNull @Positive private BigDecimal retailRate;
    private java.time.LocalDate effectiveFrom;

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
    public BigDecimal getWholesaleRate() { return wholesaleRate; }
    public void setWholesaleRate(BigDecimal v) { this.wholesaleRate = v; }
    public BigDecimal getRetailRate() { return retailRate; }
    public void setRetailRate(BigDecimal v) { this.retailRate = v; }
    public java.time.LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(java.time.LocalDate v) { this.effectiveFrom = v; }
}
