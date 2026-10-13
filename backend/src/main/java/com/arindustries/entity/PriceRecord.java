package com.arindustries.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"thickness", "product_type", "size", "effective_from"})
})
public class PriceRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String priceCode;

    @Column(length = 30, nullable = false)
    private String category;

    @Column(length = 10, nullable = false)
    private String thickness;

    @Column(name = "product_type", length = 50, nullable = false)
    private String productType;

    @Column(length = 20, nullable = false)
    private String size;

    @Column(length = 50)
    private String packaging;

    @Column(nullable = false)
    private int pcsPerBox;

    @Column(name = "wholesale_rate", precision = 10, scale = 2, nullable = false)
    private BigDecimal wholesaleRate;

    @Column(name = "retail_rate", precision = 10, scale = 2, nullable = false)
    private BigDecimal retailRate;

    @Column(name = "wholesale_box_rate", precision = 10, scale = 2)
    private BigDecimal wholesaleBoxRate;

    @Column(name = "retail_box_rate", precision = 10, scale = 2)
    private BigDecimal retailBoxRate;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(nullable = false)
    private boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPriceCode() { return priceCode; }
    public void setPriceCode(String priceCode) { this.priceCode = priceCode; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getThickness() { return thickness; }
    public void setThickness(String thickness) { this.thickness = thickness; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getPackaging() { return packaging; }
    public void setPackaging(String packaging) { this.packaging = packaging; }
    public int getPcsPerBox() { return pcsPerBox; }
    public void setPcsPerBox(int pcsPerBox) { this.pcsPerBox = pcsPerBox; }
    public BigDecimal getWholesaleRate() { return wholesaleRate; }
    public void setWholesaleRate(BigDecimal wholesaleRate) { this.wholesaleRate = wholesaleRate; }
    public BigDecimal getRetailRate() { return retailRate; }
    public void setRetailRate(BigDecimal retailRate) { this.retailRate = retailRate; }
    public BigDecimal getWholesaleBoxRate() { return wholesaleBoxRate; }
    public void setWholesaleBoxRate(BigDecimal wholesaleBoxRate) { this.wholesaleBoxRate = wholesaleBoxRate; }
    public BigDecimal getRetailBoxRate() { return retailBoxRate; }
    public void setRetailBoxRate(BigDecimal retailBoxRate) { this.retailBoxRate = retailBoxRate; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
