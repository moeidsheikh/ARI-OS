package com.arindustries.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateInventoryRequest {
    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotBlank(message = "Item name is required")
    private String itemName;

    private String category = "Modular Box";
    private String thickness = "0.8 mm";
    private String productType = "Normal Clip";
    private String size = "2M";
    private String packaging = "20 Pcs / Box";
    private int pcsPerBox = 20;
    private int currentStock = 0;
    private int minimumStock = 50;
    private String unit = "Pcs";

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
}
