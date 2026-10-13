package com.arindustries.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 30, nullable = false)
    private String itemCode;

    @Column(nullable = false, length = 255)
    private String itemName;

    @Column(length = 50, nullable = false)
    private String category;

    @Column(length = 10, nullable = false)
    private String thickness;

    @Column(length = 50, nullable = false)
    private String productType;

    @Column(length = 20, nullable = false)
    private String size;

    @Column(length = 50)
    private String packaging;

    @Column(nullable = false)
    private int pcsPerBox;

    @Column(nullable = false)
    private int currentStock;

    @Column(nullable = false)
    private int minimumStock;

    @Column(length = 20)
    private String unit = "Pcs";

    @Column(nullable = false)
    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
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
    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isLowStock() { return currentStock <= minimumStock; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
