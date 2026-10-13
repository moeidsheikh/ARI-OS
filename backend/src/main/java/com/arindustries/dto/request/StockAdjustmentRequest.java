package com.arindustries.dto.request;

import jakarta.validation.constraints.*;

public class StockAdjustmentRequest {
    @NotNull private Long inventoryId;
    @NotBlank private String adjustmentType;
    @Min(1) private int quantity;
    private String unit = "Pcs";
    private int pcsPerBox = 20;
    private String notes;

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long v) { this.inventoryId = v; }
    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String v) { this.adjustmentType = v; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int v) { this.quantity = v; }
    public String getUnit() { return unit; }
    public void setUnit(String v) { this.unit = v; }
    public int getPcsPerBox() { return pcsPerBox; }
    public void setPcsPerBox(int v) { this.pcsPerBox = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
}
