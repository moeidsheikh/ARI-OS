package com.arindustries.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardResponse {
    private BigDecimal todaySales;
    private BigDecimal monthlySales;
    private BigDecimal outstandingReceivables;
    private long pendingInvoicesCount;
    private long totalCustomers;
    private long totalProducts;
    private long lowStockCount;
    private long employeesPresentToday;
    private List<InvoiceResponse> recentInvoices;
    private List<PaymentResponse> recentPayments;
    private Map<String, Long> attendanceSummary;
    private List<AuditLogResponse> recentAuditLogs;

    public BigDecimal getTodaySales() { return todaySales; }
    public void setTodaySales(BigDecimal todaySales) { this.todaySales = todaySales; }
    public BigDecimal getMonthlySales() { return monthlySales; }
    public void setMonthlySales(BigDecimal monthlySales) { this.monthlySales = monthlySales; }
    public BigDecimal getOutstandingReceivables() { return outstandingReceivables; }
    public void setOutstandingReceivables(BigDecimal outstandingReceivables) { this.outstandingReceivables = outstandingReceivables; }
    public long getPendingInvoicesCount() { return pendingInvoicesCount; }
    public void setPendingInvoicesCount(long pendingInvoicesCount) { this.pendingInvoicesCount = pendingInvoicesCount; }
    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }
    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }
    public long getEmployeesPresentToday() { return employeesPresentToday; }
    public void setEmployeesPresentToday(long employeesPresentToday) { this.employeesPresentToday = employeesPresentToday; }
    public List<InvoiceResponse> getRecentInvoices() { return recentInvoices; }
    public void setRecentInvoices(List<InvoiceResponse> recentInvoices) { this.recentInvoices = recentInvoices; }
    public List<PaymentResponse> getRecentPayments() { return recentPayments; }
    public void setRecentPayments(List<PaymentResponse> recentPayments) { this.recentPayments = recentPayments; }
    public Map<String, Long> getAttendanceSummary() { return attendanceSummary; }
    public void setAttendanceSummary(Map<String, Long> attendanceSummary) { this.attendanceSummary = attendanceSummary; }
    public List<AuditLogResponse> getRecentAuditLogs() { return recentAuditLogs; }
    public void setRecentAuditLogs(List<AuditLogResponse> recentAuditLogs) { this.recentAuditLogs = recentAuditLogs; }
}
