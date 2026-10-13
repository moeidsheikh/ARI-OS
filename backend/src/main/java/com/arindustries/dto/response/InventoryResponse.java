package com.arindustries.dto.response;

public class InventoryResponse {
    private Long id;
    private String itemCode;
    private String itemName;
    private String category;
    private String thickness;
    private String productType;
    private String size;
    private String packaging;
    private int pcsPerBox;
    private int currentStock;
    private int minimumStock;
    private String unit;
    private boolean active;
    private boolean lowStock;
    private double currentBoxes;

    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getItemCode() { return itemCode; } public void setItemCode(String v) { this.itemCode = v; }
    public String getItemName() { return itemName; } public void setItemName(String v) { this.itemName = v; }
    public String getCategory() { return category; } public void setCategory(String v) { this.category = v; }
    public String getThickness() { return thickness; } public void setThickness(String v) { this.thickness = v; }
    public String getProductType() { return productType; } public void setProductType(String v) { this.productType = v; }
    public String getSize() { return size; } public void setSize(String v) { this.size = v; }
    public String getPackaging() { return packaging; } public void setPackaging(String v) { this.packaging = v; }
    public int getPcsPerBox() { return pcsPerBox; } public void setPcsPerBox(int v) { this.pcsPerBox = v; }
    public int getCurrentStock() { return currentStock; } public void setCurrentStock(int v) { this.currentStock = v; }
    public int getMinimumStock() { return minimumStock; } public void setMinimumStock(int v) { this.minimumStock = v; }
    public String getUnit() { return unit; } public void setUnit(String v) { this.unit = v; }
    public boolean isActive() { return active; } public void setActive(boolean v) { this.active = v; }
    public boolean isLowStock() { return lowStock; } public void setLowStock(boolean v) { this.lowStock = v; }
    public double getCurrentBoxes() { return currentBoxes; } public void setCurrentBoxes(double v) { this.currentBoxes = v; }
}
