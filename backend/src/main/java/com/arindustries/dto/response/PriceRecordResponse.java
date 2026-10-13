package com.arindustries.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PriceRecordResponse {
    private Long id;
    private String priceCode;
    private String category;
    private String thickness;
    private String productType;
    private String size;
    private String packaging;
    private int pcsPerBox;
    private BigDecimal wholesaleRate;
    private BigDecimal retailRate;
    private BigDecimal wholesaleBoxRate;
    private BigDecimal retailBoxRate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean active;
    private String notes;

    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getPriceCode() { return priceCode; } public void setPriceCode(String v) { this.priceCode = v; }
    public String getCategory() { return category; } public void setCategory(String v) { this.category = v; }
    public String getThickness() { return thickness; } public void setThickness(String v) { this.thickness = v; }
    public String getProductType() { return productType; } public void setProductType(String v) { this.productType = v; }
    public String getSize() { return size; } public void setSize(String v) { this.size = v; }
    public String getPackaging() { return packaging; } public void setPackaging(String v) { this.packaging = v; }
    public int getPcsPerBox() { return pcsPerBox; } public void setPcsPerBox(int v) { this.pcsPerBox = v; }
    public BigDecimal getWholesaleRate() { return wholesaleRate; } public void setWholesaleRate(BigDecimal v) { this.wholesaleRate = v; }
    public BigDecimal getRetailRate() { return retailRate; } public void setRetailRate(BigDecimal v) { this.retailRate = v; }
    public BigDecimal getWholesaleBoxRate() { return wholesaleBoxRate; } public void setWholesaleBoxRate(BigDecimal v) { this.wholesaleBoxRate = v; }
    public BigDecimal getRetailBoxRate() { return retailBoxRate; } public void setRetailBoxRate(BigDecimal v) { this.retailBoxRate = v; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; } public void setEffectiveFrom(LocalDate v) { this.effectiveFrom = v; }
    public LocalDate getEffectiveTo() { return effectiveTo; } public void setEffectiveTo(LocalDate v) { this.effectiveTo = v; }
    public boolean isActive() { return active; } public void setActive(boolean v) { this.active = v; }
    public String getNotes() { return notes; } public void setNotes(String v) { this.notes = v; }
}
