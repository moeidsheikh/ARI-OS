package com.arindustries.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(length = 50)
    private String category;

    @Column(length = 10)
    private String thickness;

    @Column(length = 50)
    private String productType;

    @Column(length = 20)
    private String size;

    @Column(length = 50)
    private String packaging;

    @Column(nullable = false)
    private int pcsPerBox;

    @Column(nullable = false)
    private int quantity;

    @Column(length = 10)
    private String billingUnit = "Boxes";

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal ratePerPc;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal ratePerBox;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal taxableAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal cgstPercent = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal sgstPercent = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal igstPercent = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal igstAmount = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
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
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getBillingUnit() { return billingUnit; }
    public void setBillingUnit(String billingUnit) { this.billingUnit = billingUnit; }
    public BigDecimal getRatePerPc() { return ratePerPc; }
    public void setRatePerPc(BigDecimal ratePerPc) { this.ratePerPc = ratePerPc; }
    public BigDecimal getRatePerBox() { return ratePerBox; }
    public void setRatePerBox(BigDecimal ratePerBox) { this.ratePerBox = ratePerBox; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }
    public BigDecimal getCgstPercent() { return cgstPercent; }
    public void setCgstPercent(BigDecimal cgstPercent) { this.cgstPercent = cgstPercent; }
    public BigDecimal getSgstPercent() { return sgstPercent; }
    public void setSgstPercent(BigDecimal sgstPercent) { this.sgstPercent = sgstPercent; }
    public BigDecimal getIgstPercent() { return igstPercent; }
    public void setIgstPercent(BigDecimal igstPercent) { this.igstPercent = igstPercent; }
    public BigDecimal getCgstAmount() { return cgstAmount; }
    public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }
    public BigDecimal getSgstAmount() { return sgstAmount; }
    public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }
    public BigDecimal getIgstAmount() { return igstAmount; }
    public void setIgstAmount(BigDecimal igstAmount) { this.igstAmount = igstAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
