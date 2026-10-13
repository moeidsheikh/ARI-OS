package com.arindustries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

public class BillingCalculationTest {

    @Test
    @DisplayName("Intra-state Tax Calculation: 9% CGST and 9% SGST on Taxable Amount")
    void testIntraStateTaxCalculation() {
        // 100 boxes @ 20 pcs/box = 2000 pcs @ Rs. 21.50 = Rs. 43,000 gross
        BigDecimal ratePerPc = new BigDecimal("21.50");
        int pcsPerBox = 20;
        int qtyBoxes = 100;

        BigDecimal ratePerBox = ratePerPc.multiply(BigDecimal.valueOf(pcsPerBox));
        assertEquals(new BigDecimal("430.00"), ratePerBox);

        BigDecimal grossAmount = ratePerBox.multiply(BigDecimal.valueOf(qtyBoxes));
        assertEquals(new BigDecimal("43000.00"), grossAmount);

        // 5% discount
        BigDecimal discountPercent = new BigDecimal("5.00");
        BigDecimal discount = grossAmount.multiply(discountPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("2150.00"), discount);

        BigDecimal taxable = grossAmount.subtract(discount);
        assertEquals(new BigDecimal("40850.00"), taxable);

        // CGST 9% and SGST 9%
        BigDecimal cgst = taxable.multiply(new BigDecimal("0.09")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sgst = taxable.multiply(new BigDecimal("0.09")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("3676.50"), cgst);
        assertEquals(new BigDecimal("3676.50"), sgst);

        BigDecimal rawTotal = taxable.add(cgst).add(sgst);
        assertEquals(new BigDecimal("48203.00"), rawTotal);

        BigDecimal roundedTotal = rawTotal.setScale(0, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("48203"), roundedTotal);
    }

    @Test
    @DisplayName("Inter-state Tax Calculation: 18% IGST on Taxable Amount")
    void testInterStateTaxCalculation() {
        BigDecimal taxable = new BigDecimal("50000.00");
        BigDecimal igst = taxable.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("9000.00"), igst);

        BigDecimal grandTotal = taxable.add(igst).setScale(0, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("59000"), grandTotal);
    }

    @Test
    @DisplayName("Partial Payment and Outstanding Balance deduction")
    void testPaymentAndOutstandingBalance() {
        BigDecimal grandTotal = new BigDecimal("59000.00");
        BigDecimal paymentAmount = new BigDecimal("25000.00");

        BigDecimal remainingDue = grandTotal.subtract(paymentAmount);
        assertEquals(new BigDecimal("34000.00"), remainingDue);
        assertTrue(remainingDue.compareTo(BigDecimal.ZERO) > 0);

        // Subsequent full settlement
        BigDecimal secondPayment = new BigDecimal("34000.00");
        BigDecimal finalDue = remainingDue.subtract(secondPayment);
        assertEquals(new BigDecimal("0.00"), finalDue);
    }
}
