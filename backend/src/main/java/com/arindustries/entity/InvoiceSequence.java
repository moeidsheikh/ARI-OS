package com.arindustries.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "invoice_sequence")
public class InvoiceSequence {
    @Id
    private Long id = 1L;

    @Column(name = "seq_value", nullable = false)
    private Long lastValue = 1000L;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLastValue() { return lastValue; }
    public void setLastValue(Long lastValue) { this.lastValue = lastValue; }
}
